package com.levelup.http.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.net.Uri;
import android.text.TextUtils;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.BasicHttpConfig;
import com.levelup.http.CookieManager;
import com.levelup.http.Header;
import com.levelup.http.HttpBodyParameters;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpConfig;
import com.levelup.http.HttpException;
import com.levelup.http.HttpRequestImpl;
import com.levelup.http.HttpResponse;
import com.levelup.http.InputStreamJSONObjectParser;
import com.levelup.http.InputStreamParser;
import com.levelup.http.InputStreamStringParser;
import com.levelup.http.LoggerTagged;
import com.levelup.http.MediaType;
import com.levelup.http.ParserException;
import com.levelup.http.RequestSigner;
import com.levelup.http.UploadProgressListener;
import com.levelup.http.Util;
import com.levelup.http.signed.AbstractRequestSigner;

/**
 * Base HTTP request handler used internally by {@link com.levelup.http.BaseHttpRequest BaseHttpRequest}
 *
 * @param <T> type of the data read from the HTTP response
 */
public abstract class BaseHttpRequestImpl<T> implements HttpRequestImpl<T> {
	private final Uri uri;
	protected final Map<String, String> mRequestSetHeaders = new HashMap<String, String>();
	protected final Map<String, HashSet<String>> mRequestAddHeaders = new HashMap<String, HashSet<String>>();
	private LoggerTagged mLogger;
	private HttpConfig mHttpConfig = BasicHttpConfig.instance;
	private HttpResponse httpResponse;
	private final String method;
	private final InputStreamParser<T> streamParser;
	private final RequestSigner signer;
	private UploadProgressListener mProgressListener;
	protected final HttpBodyParameters bodyParams;
	protected final Boolean followRedirect;
	private HttpErrorHandler errorHandler;

	protected static boolean isMethodWithBody(String httpMethod) {
		return !TextUtils.equals(httpMethod, "GET") && !TextUtils.equals(httpMethod, "HEAD");
	}

	protected BaseHttpRequestImpl(BaseHttpRequest.AbstractBuilder<T,?> builder) {
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
	public final InputStreamParser<T> getInputStreamParser() {
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

	@Override
	public final void prepareRequest(String userAgent) throws HttpException {
			/*
			HttpResponse resp = null;
			try {
				HttpConnectionParams.setSoTimeout(client.getParams(), config.getReadTimeout(request));
				HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT_IN_MS);
			 */
		if (!TextUtils.isEmpty(userAgent)) {
			setHeader(HTTP.USER_AGENT, userAgent);
		}

		if (null != HttpClient.getDefaultHeaders()) {
			for (Header header : HttpClient.getDefaultHeaders()) {
				setHeader(header.getName(), header.getValue());
			}
		}

		if (null != HttpClient.getCookieManager()) {
			HttpClient.getCookieManager().setCookieHeader(this);
		}

		setupBody();
		settleHttpHeaders();

		final LoggerTagged logger = getLogger();
		if (null != logger) {
			logger.v(getHttpMethod() + ' ' + getUri());
			/** TODO for (Entry<String, List<String>> header : connection.getRequestProperties().entrySet()) {
			 logger.v(header.getKey()+": "+header.getValue());
			 }*/
		}
	}

	@Override
	public void settleHttpHeaders() throws HttpException {
		if (null != signer)
			signer.sign(this);
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

	long getContentLength() {
		if (null != bodyParams) {
			return bodyParams.getContentLength();
		}
		return 0L;
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
	public void outputBody(OutputStream outputStream) throws IOException {
		final UploadProgressListener listener = getProgressListener();
		if (null != listener)
			listener.onParamUploadProgress(this, null, 0);
		if (null != bodyParams)
			bodyParams.writeBodyTo(outputStream, this, listener);
		if (null != listener)
			listener.onParamUploadProgress(this, null, 100);
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
	public void setResponse(HttpResponse resp) {
		httpResponse = resp;
		CookieManager cookieMaster = HttpClient.getCookieManager();
		if (cookieMaster != null) {
			try {
				cookieMaster.setCookieResponse(this, resp);
			} catch (IOException ignored) {
			}
		}
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
			return errorHandler.newException(this);
		return new HttpException.Builder(this);
	}

	@Override
	public HttpException.Builder newExceptionFromResponse(Throwable cause) {
		InputStream errorStream = null;
		HttpException.Builder builder = null;
		try {
			final HttpResponse response = getResponse();
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

	protected abstract InputStream getParseableErrorStream() throws IOException;

	@Override
	public void setErrorHandler(HttpErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}
}
