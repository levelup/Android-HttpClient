package com.levelup.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Basic HTTP request to be passed to {@link HttpClient}
 * @see {@link HttpRequestGet} 
 * @see {@link HttpRequestPost} 
 */
public abstract class AbstractHttpRequest implements HttpRequest {
	private final Uri uri;
	private final Map<String,String> mRequestHeaders = new HashMap<String, String>();
	private LoggerTagged mLogger;
	private HttpConfig mHttpConfig = BasicHttpConfig.instance;

	/**
	 * Constructor with a string HTTP URL
	 */
	protected AbstractHttpRequest(String url) {
		this.uri = Uri.parse(url);
	}

	/**
	 * Constructor with a {@link Uri} constructor
	 */
	protected AbstractHttpRequest(Uri uri) {
		this.uri = uri;
	}

	/**
	 * Set the {@link LoggerTagged} that will be used to send logs for this request. {@code null} is OK
	 */
	public void setLogger(LoggerTagged logger) {
		mLogger = logger;
	}

	@Override
	public LoggerTagged getLogger() {
		return mLogger;
	}

	/**
	 * Set a {@link HttpConfig}, by default {@link BasicHttpConfig} is used
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
	public void setRequestProperties(HttpURLConnection connection) throws ProtocolException {
		for (Entry<String, String> entry : mRequestHeaders.entrySet())
			connection.addRequestProperty(entry.getKey(), entry.getValue());

		if (!TextUtils.isEmpty(HttpClient.userLanguage))
			connection.setRequestProperty("Accept-Language", HttpClient.userLanguage);
	}

	@Override
	public void addHeader(String key, String value) {
		mRequestHeaders.put(key, value);
	}

	@Override
	public URL getURL() throws MalformedURLException {
		return new URL(uri.toString());
	}

	@Override
	public Uri getUri() {
		return uri;
	}

	@Override
	public void outputBody(HttpURLConnection connection) throws IOException {}

	@Override
	public void useResponse(HttpURLConnection resp) {
		CookieManager cookieMaster = HttpClient.getCookieManager();
		if (cookieMaster!=null) {
			cookieMaster.setCookieResponse(this, resp);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		String simpleName = getClass().getSimpleName();
		if (simpleName == null || simpleName.length() <= 0) {
			simpleName = getClass().getName();
			int end = simpleName.lastIndexOf('.');
			if (end > 0) {
				simpleName = simpleName.substring(end+1);
			}
		}
		sb.append(simpleName);
		sb.append('{');
		sb.append(Integer.toHexString(System.identityHashCode(this)));
		sb.append(' ');
		sb.append(uri.toString());
		sb.append('}');
		return sb.toString();
	}

	@Override
	public HttpException.Builder newException() {
		HttpException.Builder builder = new HttpException.Builder();
		builder.setHTTPRequest(this);
		return builder;
	}

	/**
	 * Handle error data returned in JSON format
	 * @param builder
	 * @param jsonData
	 * @return
	 */
	public HttpException.Builder handleJSONError(HttpException.Builder builder, JSONObject jsonData) {
		// do nothing, we don't handle JSON errors
		return builder;
	}
}
