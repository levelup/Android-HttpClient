package com.levelup.http.internal;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.http.protocol.HTTP;

import com.levelup.http.CookieManager;
import com.levelup.http.DataErrorException;
import com.levelup.http.Header;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpEngine;
import com.levelup.http.HttpException;
import com.levelup.http.HttpExceptionFactory;
import com.levelup.http.HttpRequestInfo;
import com.levelup.http.HttpResponse;
import com.levelup.http.LogManager;
import com.levelup.http.ParserException;
import com.levelup.http.RawHttpRequest;
import com.levelup.http.ResponseHandler;
import com.levelup.http.UploadProgressListener;
import com.levelup.http.signed.AbstractRequestSigner;

/**
 * Base {@link com.levelup.http.HttpEngine} with the basic stuff handled and the necessary methods an engine needs to provide
 *
 * @param <T> type of the data read from the HTTP response
 */
public abstract class AbstractHttpEngine<T,R extends HttpResponse> implements HttpEngine<T>, Closeable {
	protected final Map<String, String> requestHeaders = new HashMap<String, String>();

	protected final RawHttpRequest request;
	protected final ResponseHandler<T> responseHandler;
	protected final HttpExceptionFactory exceptionFactory;

	protected R httpResponse;

	protected static boolean isHttpError(HttpResponse httpResponse) throws IOException {
		return httpResponse.getResponseCode() < 200 || httpResponse.getResponseCode() >= 400;
	}

	public AbstractHttpEngine(RawHttpRequest request, ResponseHandler<T> responseHandler, HttpExceptionFactory exceptionFactory) {
		this.request = request;
		this.responseHandler = responseHandler;
		this.exceptionFactory = exceptionFactory;

		for (Header header : request.getAllHeaders()) {
			requestHeaders.put(header.getName(), header.getValue());
		}
	}

	@Override
	public final ResponseHandler<T> getResponseHandler() {
		return responseHandler;
	}

	@Override
	public final HttpRequestInfo getHttpRequest() {
		return request;
	}

	@Override
	public final HttpExceptionFactory getExceptionFactory() {
		return exceptionFactory;
	}

	/**
	 * Set all internal variables and sign the query if needed
	 * <p>Usually you don't need to call this yourself, the engine will do it</p>
	 * @throws HttpException
	 */
	public final void prepareEngine() throws HttpException {
			/*
			HttpResponse resp = null;
			try {
				HttpConnectionParams.setSoTimeout(client.getParams(), config.getReadTimeout(request));
				HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT_IN_MS);
			 */
		if (null != HttpClient.getCookieManager()) {
			HttpClient.getCookieManager().setCookieHeader(this);
		}

		request.settleHttpHeaders();

		final long contentLength;
		if (null != request.getBodyParameters()) {
			setHeader(HTTP.CONTENT_TYPE, request.getBodyParameters().getContentType());
			contentLength = request.getBodyParameters().getContentLength();
		} else {
			contentLength = 0L;
		}
		setContentLength(contentLength);

		if (null != request.getRequestSigner())
			request.getRequestSigner().sign(this);

		setHeadersAndConfig();
	}

	protected void setContentLength(long contentLength) {
		setHeader(HTTP.CONTENT_LEN, Long.toString(contentLength));
	}

	/**
	 * Set the HTTP headers on the internal connection, the follow redirect setting and the timeout
	 * @throws HttpException
	 */
	protected abstract void setHeadersAndConfig();

	@Override
	public final void setHeader(String name, String value) {
		if (null == value)
			requestHeaders.remove(name);
		else
			requestHeaders.put(name, value);
	}

	@Override
	public String getHeader(String name) {
		return requestHeaders.get(name);
	}

	protected void outputBody(OutputStream outputStream, HttpRequestInfo requestInfo) throws IOException {
		final UploadProgressListener listener = request.getProgressListener();
		if (null != listener)
			listener.onParamUploadProgress(requestInfo, null, 0);
		if (null != request.getBodyParameters())
			request.getBodyParameters().writeBodyTo(outputStream, requestInfo, listener);
		if (null != listener)
			listener.onParamUploadProgress(requestInfo, null, 100);
	}

	/**
	 * Do the query on the network and return the internal {@link com.levelup.http.HttpResponse}
	 * @return
	 * @throws HttpException
	 */
	protected abstract R queryResponse() throws HttpException;

	protected T responseToResult(R response) throws ParserException, IOException {
		return responseHandler.contentParser.transformData(response, this);
	}

	@Override
	public final T call() throws HttpException {
		prepareEngine();

		R httpResponse = queryResponse();
		try {
			return responseToResult(httpResponse);

		} catch (ParserException e) {
			throw exceptionToHttpException(e).build();

		} catch (IOException e) {
			throw exceptionToHttpException(e).build();
		}
	}

	@Override
	public void close() throws IOException {
		if (null != httpResponse)
			httpResponse.disconnect();
	}

	protected void setRequestResponse(R httpResponse) {
		this.httpResponse = httpResponse;
		request.setResponse(httpResponse);

		CookieManager cookieMaster = HttpClient.getCookieManager();
		if (cookieMaster != null) {
			try {
				cookieMaster.setCookieResponse(this);
			} catch (IOException ignored) {
			}
		}
	}

	@Override
	public R getHttpResponse() {
		return httpResponse;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		String simpleName = getClass().getSimpleName();
		if (simpleName == null || simpleName.length() <= 0) {
			simpleName = getClass().getName();
			int end = simpleName.lastIndexOf('.');
			if (end > 0) {
				simpleName = simpleName.substring(end + 1);
			}
		}
		sb.append(simpleName);
		sb.append('{');
		sb.append(Integer.toHexString(System.identityHashCode(this)));
		sb.append(' ');
		if (request.getRequestSigner() instanceof AbstractRequestSigner) {
			sb.append(" for ").append(((AbstractRequestSigner) request.getRequestSigner()).getOAuthUser());
		}
		sb.append('}');
		return sb.toString();
	}

	protected HttpException.Builder exceptionToHttpException(Exception e) throws HttpException {
		HttpException.Builder builder = exceptionFactory.newException(httpResponse);

		if (e instanceof DataErrorException) {
			DataErrorException cause = (DataErrorException) e;
			if (cause.errorContent instanceof Exception)
				throw exceptionToHttpException((Exception) cause.errorContent).build();

			builder.setErrorMessage("interrupted");
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_DATA_MSG);
			return builder;
		}

		else if (e instanceof InterruptedException) {
			builder.setErrorMessage("interrupted");
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_HTTP);
			return builder;
		}

		else if (e instanceof ExecutionException) {
			if (e.getCause() instanceof Exception && e.getCause()!=e)
				return exceptionToHttpException((Exception) e.getCause());
			else {
				builder.setErrorMessage("execution error");
				builder.setCause(e.getCause());
				builder.setErrorCode(HttpException.ERROR_HTTP);
				return builder;
			}
		}

		else if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
			LogManager.getLogger().d("timeout for "+request);
			builder.setErrorMessage("Timeout error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_TIMEOUT);
			return builder;
		}

		else if (e instanceof ProtocolException) {
			LogManager.getLogger().d("bad method for " + request + ' ' + e.getMessage());
			builder.setErrorMessage("Method error " + e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_HTTP);
			return builder;
		}

		else if (e instanceof IOException) {
			LogManager.getLogger().d("i/o error for " + request + ' ' + e.getMessage());
			builder.setErrorMessage("IO error " + e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			return builder;
		}

		else if (e instanceof ParserException) {
			LogManager.getLogger().i("incorrect data for " + request);
			if (e.getCause() instanceof HttpException)
				throw (HttpException) e.getCause();

			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_PARSER);
			return builder;
		}

		else if (e instanceof SecurityException) {
			LogManager.getLogger().w("security error for " + request + ' ' + e);
			builder.setErrorMessage("Security error " + e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			return builder;
		}

		else {
			LogManager.getLogger().w("unknown error for " + request + ' ' + e);
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_HTTP);
		}

		return builder;
	}
}
