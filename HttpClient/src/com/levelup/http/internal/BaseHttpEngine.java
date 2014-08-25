package com.levelup.http.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.JsonParseException;
import com.levelup.http.BaseHttpRequest;
import com.levelup.http.BasicHttpConfig;
import com.levelup.http.CookieManager;
import com.levelup.http.DataErrorException;
import com.levelup.http.Header;
import com.levelup.http.HttpBodyParameters;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpConfig;
import com.levelup.http.HttpEngine;
import com.levelup.http.HttpException;
import com.levelup.http.HttpExceptionCreator;
import com.levelup.http.HttpRequest;
import com.levelup.http.HttpRequestInfo;
import com.levelup.http.HttpResponse;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.InputStreamJSONObjectParser;
import com.levelup.http.InputStreamStringParser;
import com.levelup.http.LogManager;
import com.levelup.http.LoggerTagged;
import com.levelup.http.MediaType;
import com.levelup.http.ParserException;
import com.levelup.http.RequestSigner;
import com.levelup.http.UploadProgressListener;
import com.levelup.http.Util;
import com.levelup.http.parser.ResponseParser;
import com.levelup.http.signed.AbstractRequestSigner;

/**
 * Base HTTP request handler used internally by {@link com.levelup.http.BaseHttpRequest BaseHttpRequest}
 *
 * @param <T> type of the data read from the HTTP response
 */
public abstract class BaseHttpEngine<T,R extends HttpResponse> implements HttpEngine<T>, ImmutableHttpRequest {
	private final Uri uri;
	protected final Map<String, String> mRequestSetHeaders = new HashMap<String, String>();
	protected final Map<String, HashSet<String>> mRequestAddHeaders = new HashMap<String, HashSet<String>>();
	private LoggerTagged mLogger;
	private HttpConfig mHttpConfig = BasicHttpConfig.instance;
	private R httpResponse;
	private final String method;
	private final ResponseParser<T,?> streamParser;
	private final RequestSigner signer;
	private UploadProgressListener mProgressListener;
	protected final HttpBodyParameters bodyParams;
	protected final Boolean followRedirect;
	private HttpErrorHandler errorHandler;

	protected static boolean isMethodWithBody(String httpMethod) {
		return !TextUtils.equals(httpMethod, "GET") && !TextUtils.equals(httpMethod, "HEAD");
	}

	protected BaseHttpEngine(BaseHttpRequest.AbstractBuilder<T, ?> builder) {
		this.uri = builder.getUri();
		this.method = builder.getHttpMethod();
		this.streamParser = builder.getInputStreamParser();
		this.bodyParams = builder.getBodyParams();
		this.signer = builder.getSigner();
		this.followRedirect = builder.getFollowRedirect();
	}

	@Override
	public final String getHttpMethod() {
		return method;
	}

	@Override
	public final ResponseParser<T,?> getResponseParser() {
		return streamParser;
	}

	/**
	 * Set the {@link com.levelup.http.LoggerTagged} that will be used to send logs for this request. {@code null} is OK
	 */
	@Override
	public void setLogger(LoggerTagged logger) {
		mLogger = logger;
	}

	@Override
	public LoggerTagged getLogger() {
		return mLogger;
	}

	/**
	 * Set a {@link com.levelup.http.HttpConfig}, by default {@link com.levelup.http.BasicHttpConfig} is used
	 */
	@Override
	public void setHttpConfig(HttpConfig config) {
		mHttpConfig = config;
	}

	@Override
	public HttpConfig getHttpConfig() {
		return mHttpConfig;
	}

	public final void prepareRequest(HttpRequest request) throws HttpException {
			/*
			HttpResponse resp = null;
			try {
				HttpConnectionParams.setSoTimeout(client.getParams(), config.getReadTimeout(request));
				HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT_IN_MS);
			 */
		if (!TextUtils.isEmpty(HttpClient.getUserAgent())) {
			setHeader(HTTP.USER_AGENT, HttpClient.getUserAgent());
		}

		if (null != HttpClient.getDefaultHeaders()) {
			for (Header header : HttpClient.getDefaultHeaders()) {
				setHeader(header.getName(), header.getValue());
			}
		}

		if (null != HttpClient.getCookieManager()) {
			HttpClient.getCookieManager().setCookieHeader(request);
		}

		setupBody();
		settleHttpHeaders(request);
	}

	@Override
	public void settleHttpHeaders(HttpRequest request) throws HttpException {
		request.settleHttpHeaders();

		if (null != signer)
			signer.sign(request);
	}

	@Override
	public void addHeader(String key, String value) {
		HashSet<String> values = mRequestAddHeaders.get(key);
		if (null == values) {
			values = new HashSet<String>();
			mRequestAddHeaders.put(key, values);
		}
		values.add(value);
	}

	@Override
	public void setHeader(String key, String value) {
		mRequestAddHeaders.remove(key);
		if (null == value)
			mRequestSetHeaders.remove(key);
		else
			mRequestSetHeaders.put(key, value);
	}

	@Override
	public String getHeader(String name) {
		if (mRequestSetHeaders.containsKey(name))
			return mRequestSetHeaders.get(name);
		if (mRequestAddHeaders.containsKey(name) && !mRequestAddHeaders.get(name).isEmpty())
			return mRequestAddHeaders.get(name).toArray(EMPTY_STRINGS)[0];
		return null;
	}

	@Override
	public String getContentType() {
		if (null != bodyParams) {
			return bodyParams.getContentType();
		}
		return null;
	}

	private static final String[] EMPTY_STRINGS = {};

	@Override
	public Header[] getAllHeaders() {
		List<Header> headers = null == HttpClient.getDefaultHeaders() ? new ArrayList<Header>() : new ArrayList<Header>(Arrays.asList(HttpClient.getDefaultHeaders()));
		for (Entry<String, String> setHeader : mRequestSetHeaders.entrySet()) {
			headers.add(new Header(setHeader.getKey(), setHeader.getValue()));
		}
		for (Entry<String, HashSet<String>> entries : mRequestAddHeaders.entrySet()) {
			for (String entry : entries.getValue()) {
				headers.add(new Header(entries.getKey(), entry));
			}
		}
		return headers.toArray(new Header[headers.size()]);
	}

	@Override
	public final Uri getUri() {
		return uri;
	}

	@Override
	public void outputBody(OutputStream outputStream, HttpRequestInfo requestInfo) throws IOException {
		final UploadProgressListener listener = getProgressListener();
		if (null != listener)
			listener.onParamUploadProgress(requestInfo, null, 0);
		if (null != bodyParams)
			bodyParams.writeBodyTo(outputStream, requestInfo, listener);
		if (null != listener)
			listener.onParamUploadProgress(requestInfo, null, 100);
	}

	@Override
	public void setProgressListener(UploadProgressListener listener) {
		this.mProgressListener = listener;
	}

	@Override
	public UploadProgressListener getProgressListener() {
		return mProgressListener;
	}

	@Override
	public boolean hasBody() {
		return null != bodyParams;
	}

	@Override
	public <P> P parseRequest(ResponseParser<P, ?> parser, HttpRequest request) throws HttpException {
		InputStream is = getInputStream(request, parser);
		if (null != is)
			try {
				return parser.parseResponse(this);
			} catch (ParserException e) {
				throw exceptionToHttpException(request, e).build();

			} catch (DataErrorException e) {
				throw exceptionToHttpException(request, e).build();

			} catch (IOException e) {
				throw exceptionToHttpException(request, e).build();

			} finally {
				try {
					is.close();
				} catch (NullPointerException ignored) {
					// okhttp 2.0 bug https://github.com/square/okhttp/issues/690
				} catch (IOException ignored) {
				}
			}

		return null;
	}

	protected void setRequestResponse(HttpRequest request, R httpResponse) {
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
	public HttpResponse getResponse() {
		return httpResponse;
	}

	@Override
	public RequestSigner getRequestSigner() {
		return signer;
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
		if (signer instanceof AbstractRequestSigner) {
			sb.append(" for ").append(((AbstractRequestSigner) signer).getOAuthUser());
		}
		sb.append('}');
		return sb.toString();
	}

	@Override
	public HttpException.Builder newException() {
		if (null!=errorHandler)
			return errorHandler.newException();
		throw new IllegalAccessError("missing errorHandler");
	}

	@Override
	public HttpException.Builder newExceptionFromResponse(Throwable cause) {
		InputStream errorStream = null;
		HttpException.Builder builder = null;
		try {
			final HttpResponse response = getHttpResponse();
			builder = newException();
			builder.setErrorCode(HttpException.ERROR_HTTP);
			builder.setHTTPResponse(response);
			builder.setCause(cause);

			MediaType type = MediaType.parse(response.getContentType());
			if (Util.MediaTypeJSON.equalsType(type)) {
				errorStream = getParseableErrorStream();
				JSONObject jsonData = InputStreamJSONObjectParser.instance.parseInputStream(errorStream, this);
				builder.setErrorMessage(jsonData.toString());
				if (null!=errorHandler)
					builder = errorHandler.handleJSONError(builder, jsonData);
			} else if (null == type || "text".equals(type.type())) {
				errorStream = getParseableErrorStream();
				String errorData = InputStreamStringParser.instance.parseInputStream(errorStream, this);
				builder.setErrorMessage(errorData);
			}
		} catch (IOException ignored) {
		} catch (ParserException ignored) {
		} catch (Exception e) {
			LogManager.getLogger().w("unknown HTTP error",e);
		} finally {
			if (null != errorStream) {
				try {
					errorStream.close();
				} catch (NullPointerException ignored) {
					// okhttp 2.0 bug https://github.com/square/okhttp/issues/690
				} catch (IOException ignored) {
				}
			}
		}
		return builder;
	}
	protected HttpException.Builder exceptionToHttpException(HttpExceptionCreator request, Exception e) throws HttpException {
		if (e instanceof DataErrorException) {
			DataErrorException cause = (DataErrorException) e;
			if (cause.errorContent instanceof Exception)
				throw exceptionToHttpException(request, (Exception) cause.errorContent).build();

			if (cause.errorContent instanceof InputStream) {
				return newExceptionFromResponse(e.getCause());
			}
		}

		if (e instanceof InterruptedException) {
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("interrupted");
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_HTTP);
			return builder;
		}

		if (e instanceof ExecutionException) {
			if (e.getCause() instanceof Exception && e.getCause()!=e)
				return exceptionToHttpException(request, (Exception) e.getCause());
			else {
				HttpException.Builder builder = request.newException();
				builder.setErrorMessage("execution error");
				builder.setCause(e.getCause());
				builder.setErrorCode(HttpException.ERROR_HTTP);
				return builder;
			}
		}

		if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
			LogManager.getLogger().d("timeout for "+request);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Timeout error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_TIMEOUT);
			return builder;
		}

		if (e instanceof ProtocolException) {
			LogManager.getLogger().d("bad method for " + request + ' ' + e.getMessage());
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Method error " + e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_HTTP);
			return builder;
		}

		if (e instanceof IOException) {
			LogManager.getLogger().d("i/o error for " + request + ' ' + e.getMessage());
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("IO error " + e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			return builder;
		}

		if (e instanceof ParserException) {
			LogManager.getLogger().i("incorrect data for " + request);
			if (e.getCause() instanceof HttpException)
				throw (HttpException) e.getCause();

			HttpException.Builder builder = request.newException();
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_PARSER);
			return builder;
		}

		if (e instanceof JsonParseException) {
			LogManager.getLogger().i("incorrect data for " + request);
			HttpException.Builder builder = request.newException();
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_PARSER);
			return builder;
		}

		if (e instanceof SecurityException) {
			LogManager.getLogger().w("security error for " + request + ' ' + e);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Security error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			return builder;
		}

		LogManager.getLogger().w("unknown error for " + request + ' ' + e);
		HttpException.Builder builder = request.newException();
		builder.setCause(e);
		builder.setErrorCode(HttpException.ERROR_HTTP);
		return builder;
	}


	@Override
	public HttpRequestInfo getHttpRequestInfo() {
		return this;
	}

	protected abstract InputStream getParseableErrorStream() throws Exception;

	@Override
	public void setErrorHandler(HttpErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}
}
