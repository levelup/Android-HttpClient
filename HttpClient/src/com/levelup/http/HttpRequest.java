package com.levelup.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
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
public abstract class HttpRequest {
	private final Uri url;
	private final Map<String,String> mRequestHeaders = new HashMap<String, String>();
	private LoggerTagged mLogger;
	private HttpConfig mHttpConfig = BasicHttpConfig.instance;

	/**
	 * Constructor with a string HTTP URL
	 */
	protected HttpRequest(String url) {
		this.url = Uri.parse(url);
	}

	/**
	 * Constructor with a {@link Uri} constructor
	 */
	protected HttpRequest(Uri uri) {
		this.url = uri;
	}

	/**
	 * Set the {@link LoggerTagged} that will be used to send logs for this request. {@code null} is OK
	 */
	public void setLogger(LoggerTagged logger) {
		mLogger = logger;
	}

	/**
	 * The {@link LoggerTagged} that is used to send logs for this request. May be {@code null}
	 */
	public LoggerTagged getLogger() {
		return mLogger;
	}

	/**
	 * Set a {@link HttpConfig}, by default {@link BasicHttpConfig} is used
	 */
	public void setHttpConfig(HttpConfig config) {
		mHttpConfig = config;
	}

	/**
	 * Get the {@link HttpConfig} used for this request
	 */
	public HttpConfig getHttpConfig() {
		return mHttpConfig;
	}

	public void setRequestProperties(HttpURLConnection connection) throws ProtocolException {
		for (Entry<String, String> entry : mRequestHeaders.entrySet())
			connection.addRequestProperty(entry.getKey(), entry.getValue());

		if (!TextUtils.isEmpty(HttpClient.userLanguage))
			connection.setRequestProperty("Accept-Language", HttpClient.userLanguage);
	}

	public void addHeader(String key, String value) {
		mRequestHeaders.put(key, value);
	}

	public URL getURL() throws MalformedURLException {
		return new URL(url.toString());
	}

	public URI getURI() {
		return URI.create(url.toString());
	}

	public Uri getUri() {
		return url;
	}

	/**
	 * Write the HTTP request body
	 * @param connection
	 * @throws IOException
	 */
	public void outputBody(HttpURLConnection connection) throws IOException {}

	/**
	 * Use data from the response for this request, even if the response is an error
	 * @param resp
	 */
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
		sb.append(url.toString());
		sb.append('}');
		return sb.toString();
	}

	public HttpException.Builder newException() {
		HttpException.Builder builder = new HttpException.Builder();
		builder.setHTTPRequest(this);
		return builder;
	}

	public HttpException.Builder handleJSONError(HttpException.Builder builder, JSONObject jsonData) {
		// do nothing, we don't handle JSON errors
		return builder;
	}
}
