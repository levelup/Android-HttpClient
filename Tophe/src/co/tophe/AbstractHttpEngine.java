package co.tophe;

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

import android.annotation.SuppressLint;
import android.net.TrafficStats;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import co.tophe.log.LogManager;
import co.tophe.parser.ParserException;
import co.tophe.signed.AbstractRequestSigner;

/**
 * Base {@link HttpEngine} with the basic stuff handled and the necessary methods an engine needs to provide
 *
 * @param <T> type of the data read from the HTTP response
 */
public abstract class AbstractHttpEngine<T,R extends HttpResponse, SE extends ServerException> implements HttpEngine<T, SE>, Closeable {
	protected final Map<String, String> requestHeaders = new HashMap<String, String>();

	protected final RawHttpRequest request;
	protected final ResponseHandler<T, SE> responseHandler;
	protected final int threadStatsTag;

	protected R httpResponse;

	public static boolean isHttpError(HttpResponse httpResponse) throws IOException {
		return httpResponse.getResponseCode() < 200 || httpResponse.getResponseCode() >= 400;
	}

	public AbstractHttpEngine(Builder<T, SE> builder) {
		this.request = builder.getHttpRequest();
		this.responseHandler = builder.getResponseHandler();
		this.threadStatsTag = builder.getThreadStatsTag();

		for (Header header : request.getAllHeaders()) {
			requestHeaders.put(header.getName(), header.getValue());
		}

		String userAgent = requestHeaders.get(HTTP.USER_AGENT);
		if (null!=userAgent) {
			String engineSignature = getEngineSignature();
			if (null!=engineSignature) {
				setHeader(HTTP.USER_AGENT, userAgent + ' ' + engineSignature);
			}
		}
	}

	@NonNull
	@Override
	public final ResponseHandler<T,SE> getResponseHandler() {
		return responseHandler;
	}

	@Override
	public final HttpRequestInfo getHttpRequest() {
		return request;
	}

	/**
	 * Set all internal variables and sign the query if needed
	 * <p>Usually you don't need to call this yourself, the engine will do it</p>
	 * @throws HttpSignException
	 */
	public final void prepareEngine() throws HttpSignException {
			/*
			HttpResponse resp = null;
			try {
				HttpConnectionParams.setSoTimeout(client.getParams(), config.getReadTimeout(request));
				HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT_IN_MS);
			 */
		if (null != TopheClient.getCookieManager()) {
			TopheClient.getCookieManager().setHttpEngineCookies(this);
		}

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

	/**
	 * @return a String representing the engine, to put in the user-agent
	 */
	protected abstract String getEngineSignature();

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
	 * Do the query on the network and return the internal {@link HttpResponse}, do not parse the result here
	 * @return
	 * @throws HttpException
	 */
	protected abstract R queryResponse() throws HttpException, SE;

	protected T responseToResult(R response) throws ParserException, IOException {
		return responseHandler.contentParser.transformData(response, this);
	}

	@SuppressLint("NewApi")
	@Override
	public final T call() throws HttpException, SE {
		if (0 != threadStatsTag) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				TrafficStats.setThreadStatsTag(threadStatsTag);
		}

		prepareEngine();

		R httpResponse = queryResponse();
		try {
			String expectedMimeType = request.getHeader(HttpRequest.HEADER_ACCEPT);
			if (!TextUtils.isEmpty(expectedMimeType)) {
				MediaType expectedType = MediaType.parse(expectedMimeType);
				if (null!=expectedType && !expectedType.equalsType(MediaType.parse(httpResponse.getContentType()))) {
					HttpMimeException.Builder builder = new HttpMimeException.Builder(request, httpResponse);
					builder.setErrorMessage("Expected '" + expectedMimeType + "' got '" + httpResponse.getContentType());
					throw builder.build();
				}
			}

			return responseToResult(httpResponse);

		} catch (ParserException e) {
			throw exceptionToHttpException(e).build();

		} catch (IOException e) {
			throw exceptionToHttpException(e).build();

		} finally {
			if (0 != threadStatsTag) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					TrafficStats.clearThreadStatsTag();
			}
		}
	}

	@Override
	public void close() throws IOException {
		if (null != httpResponse)
			httpResponse.disconnect();
	}

	protected void setRequestResponse(R httpResponse) {
		this.httpResponse = httpResponse;
		responseHandler.onHttpResponse(request, httpResponse);

		CookieManager cookieMaster = TopheClient.getCookieManager();
		if (cookieMaster != null) {
			try {
				cookieMaster.onCookiesReceived(this);
			} catch (IOException ignored) {
			}
		}
	}

	@Override
	@Nullable
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

	protected HttpException.AbstractBuilder<? extends HttpException, ?> exceptionToHttpException(Exception e) throws HttpException {
		if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
			LogManager.getLogger().d("timeout for "+request);
			return new HttpTimeoutException.Builder(request, httpResponse)
					.setErrorMessage("Timeout error " + e.getMessage())
					.setCause(e);
		}

		else if (e instanceof ProtocolException) {
			LogManager.getLogger().d("bad method for " + request + ' ' + e.getMessage());
			return new HttpProtocolException.Builder(request, httpResponse, (ProtocolException) e)
					.setErrorMessage("Method error " + e.getMessage());
		}

		else if (e instanceof IOException) {
			LogManager.getLogger().d("i/o error for " + request + ' ' + e.getMessage());
			return new HttpIOException.Builder(request, httpResponse)
					.setErrorMessage("IO error " + e.getMessage())
					.setCause(e);
		}

		else if (e instanceof ParserException) {
			LogManager.getLogger().i("incorrect data for " + request);
			if (e.getCause() instanceof HttpException)
				throw (HttpException) e.getCause();

			return new HttpDataParserException.Builder(request, httpResponse, (ParserException) e);
		}

		else if (e instanceof SecurityException) {
			LogManager.getLogger().w("security error for " + request + ' ' + e);
			return new TopheNetworkException.Builder(request, httpResponse)
					.setErrorMessage("Security error " + e.getMessage())
					.setCause(e);
		}

		else if (e instanceof ExecutionException && e.getCause() instanceof Exception && e.getCause()!=e) {
			return exceptionToHttpException((Exception) e.getCause());
		}

		HttpException.Builder builder = new HttpException.Builder(request, httpResponse);
		builder.setCause(e);

		if (e instanceof InterruptedException) {
			builder.setErrorMessage("interrupted");

		} else if (e instanceof ExecutionException) {

			builder.setErrorMessage("execution error");
			builder.setCause(e.getCause());
		}

		else {
			LogManager.getLogger().w("unknown error for " + request, e);
		}

		return builder;
	}
}
