package com.levelup.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.text.TextUtils;

import com.levelup.http.HttpException.Builder;

/**
 * Basic HTTP request to be passed to {@link HttpClient}
 * @see {@link HttpRequestGet} 
 * @see {@link HttpRequestPost} 
 */
public abstract class AbstractHttpRequest implements HttpRequest {
	private final Uri uri;
	private final Map<String,String> mRequestSetHeaders = new HashMap<String, String>();
	private final Map<String, HashSet<String>> mRequestAddHeaders = new HashMap<String, HashSet<String>>();
	private LoggerTagged mLogger;
	private HttpConfig mHttpConfig = BasicHttpConfig.instance;
	private HttpURLConnection httpResponse;

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
	public void settleHttpHeaders() throws HttpException {
		// do nothing
	}
	
	@Override
	public void setConnectionProperties(HttpURLConnection connection) throws ProtocolException {
		for (Entry<String, String> entry : mRequestSetHeaders.entrySet())
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		for (Entry<String, HashSet<String>> entry : mRequestAddHeaders.entrySet()) {
			for (String value : entry.getValue())
				connection.addRequestProperty(entry.getKey(), value);
		}
	}

	@Override
	public void addHeader(String key, String value) {
		HashSet<String> values = mRequestAddHeaders.get(key);
		if (null==values) {
			values = new HashSet<String>();
			mRequestAddHeaders.put(key, values);
		}
		values.add(value);
	}

	@Override
	public void setHeader(String key, String value) {
		mRequestAddHeaders.remove(key);
		if (null==value)
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

	private static final Header[] EMPTY_HEADERS = new Header[0];
	private static final String[] EMPTY_STRINGS = {};

	public Header[] getAllHeaders() {
		List<Header> headers = null==HttpClient.getDefaultHeaders() ? new ArrayList<Header>() : new ArrayList<Header>(Arrays.asList(HttpClient.getDefaultHeaders()));
		for (Entry<String, String> setHeader : mRequestSetHeaders.entrySet()) {
			headers.add(new Header(setHeader.getKey(), setHeader.getValue()));
		}
		for (Entry<String, HashSet<String>> entries : mRequestAddHeaders.entrySet()) {
			for (String entry : entries.getValue()) {
				headers.add(new Header(entries.getKey(), entry));
			}
		}
		return headers.toArray(EMPTY_HEADERS);
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
	public void setResponse(HttpURLConnection resp) {
		httpResponse = resp;
		CookieManager cookieMaster = HttpClient.getCookieManager();
		if (cookieMaster!=null) {
			cookieMaster.setCookieResponse(this, resp);
		}
	}
	
	@Override
	public HttpURLConnection getResponse() {
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
		return new HttpException.Builder(this);
	}
	
	@Override
	public HttpException.Builder newExceptionFromResponse(HttpURLConnection response) {
		InputStream errorStream = null;
		Builder builder = null;
		StringBuilder sb = null;
		try {
			builder = newException();
			builder.setErrorCode(HttpException.ERROR_HTTP);
			builder.setHTTPResponse(response);

			errorStream = response.getErrorStream();
			if ("deflate".equals(response.getContentEncoding()) && !(errorStream instanceof InflaterInputStream))
				errorStream = new InflaterInputStream(errorStream);
			if ("gzip".equals(response.getContentEncoding()) && !(errorStream instanceof GZIPInputStream))
				errorStream = new GZIPInputStream(errorStream);
			BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"), 1250);
			sb = new StringBuilder(response.getContentLength() > 0 ? response.getContentLength() : 64);
			String line;
			while ((line = reader.readLine())!=null) {
				if (!TextUtils.isEmpty(line)) {
					if (sb.length()!=0)
						sb.append('\n');
					sb.append(line);
				}
			}

			if (response.getContentType()!=null && response.getContentType().startsWith("application/json")) {
				JSONObject jsonData = new JSONObject(sb.toString());
				builder = handleJSONError(builder, jsonData);
			}

			builder.setErrorMessage(sb.toString());
		} catch (UnsupportedEncodingException ignored) {
		} catch (JSONException e) {
			HttpException.Builder b = newException();
			b.setErrorCode(HttpException.ERROR_JSON);
			b.setErrorMessage(sb.length()==0 ? "json error" : sb.toString());
			b.setCause(builder.build());
			builder = b;
		} catch (IOException ignored) {
		} finally {
			if (null!=errorStream) {
				try {
					errorStream.close();
				} catch (NullPointerException ignored) {
					// okhttp 2.0 bug https://github.com/square/okhttp/issues/690
				} catch (IOException ignored) {
				}
				errorStream = null;
			}
		}
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
