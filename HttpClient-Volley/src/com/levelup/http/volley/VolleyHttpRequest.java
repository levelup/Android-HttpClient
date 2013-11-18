package com.levelup.http.volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.protocol.HTTP;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;
import com.levelup.http.HttpRequestPost;

/**
 * A wrapper to use a {@link com.levelup.http.HttpRequest HttpRequest} as a {@link com.android.volley.Request Volley Request}
 */
public class VolleyHttpRequest extends Request<String> {
	private final HttpRequest request;
	private final Listener<String> requestListener;
	private DummyHttpURLConnection dummyConn;

	private static class DummyHttpURLConnection extends HttpURLConnection {
		private ByteArrayOutputStream outputData;
		private final Map<String,String> mRequestSetHeaders = new HashMap<String, String>();
		private final Map<String, HashSet<String>> mRequestAddHeaders = new HashMap<String, HashSet<String>>();

		protected DummyHttpURLConnection(URL url) {
			super(url);
		}

		@Override
		public void disconnect() {
			throw new IllegalAccessError();
		}

		@Override
		public boolean usingProxy() {
			throw new IllegalAccessError();
		}

		@Override
		public void connect() throws IOException {
			throw new IllegalAccessError();
		}

		@Override
		public void setRequestProperty(String field, String newValue) {
			mRequestAddHeaders.remove(field);
			if (null==newValue)
				mRequestSetHeaders.remove(field);
			else
				mRequestSetHeaders.put(field, newValue);
		}

		@Override
		public void addRequestProperty(String field, String newValue) {
			HashSet<String> values = mRequestAddHeaders.get(field);
			if (null==values) {
				values = new HashSet<String>();
				mRequestAddHeaders.put(field, values);
			}
			values.add(newValue);
		}

		@Override
		public String getRequestProperty(String field) {
			String result = mRequestSetHeaders.get(field);
			if (null!=result)
				return result;
			HashSet<String> values = mRequestAddHeaders.get(field);
			if (null!=values)
				for (String value : values) {
					return value;
				}
			return null;
		}

		@Override
		public Map<String, List<String>> getRequestProperties() {
			Map<String, List<String>> h = new HashMap<String, List<String>>(mRequestAddHeaders.size() + mRequestSetHeaders.size());
			for (Entry<String, String> set : mRequestSetHeaders.entrySet()) {
				List<String> value = new ArrayList<String>(1);
				value.add(set.getValue());
				h.put(set.getKey(), value);
			}
			for (Entry<String, HashSet<String>> entries : mRequestAddHeaders.entrySet()) {
				List<String> list = h.get(entries.getKey());
				if (null == list) {
					list = new ArrayList<String>(entries.getValue().size());
					h.put(entries.getKey(), list);
				}
				list.addAll(entries.getValue());
			}
			return h;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			if (null==outputData)
				outputData = new ByteArrayOutputStream(100);
			return outputData;
		}
	}

	private class WrappedRetryPolicy implements RetryPolicy {
		private final RetryPolicy wrapped;
		public WrappedRetryPolicy(RetryPolicy wrapped) {
			if (null==wrapped) throw new NullPointerException();
			this.wrapped = wrapped;
		}

		@Override
		public int getCurrentTimeout() {
			if (null!=request.getHttpConfig()) {
				assertHeaderBody();
				int readTimeout = request.getHttpConfig().getReadTimeout(request);
				if (readTimeout >= 0)
					return readTimeout;
			}
			return wrapped.getCurrentTimeout();
		}

		@Override
		public int getCurrentRetryCount() {
			return wrapped.getCurrentRetryCount();
		}

		@Override
		public void retry(VolleyError error) throws VolleyError {
			wrapped.retry(error);
		}
	}

	/**
	 * Create a {@link com.levelup.http.HttpRequest HttpRequest} wrapper that can be used with Volley's {@link RequestQueue#add(Request)} 
	 * @param request The request to wrap
	 * @param requestListener The success listener
	 * @param errorListener The error listener
	 */
	public VolleyHttpRequest(HttpRequest request, Listener<String> requestListener, ErrorListener errorListener) {
		super(getVolleyMethod(request), request.getUri().toString(), errorListener);
		this.request = request;
		this.requestListener = requestListener;
	}

	@Override
	public void setRetryPolicy(RetryPolicy retryPolicy) {
		super.setRetryPolicy(new WrappedRetryPolicy(retryPolicy));
	}

	private static int getVolleyMethod(HttpRequest request) {
		if (null==request) throw new NullPointerException();
		if (request instanceof HttpRequestPost)
			return Method.POST;
		return Method.GET;
	}

	/**
	 * Make sure the HTTP headers and body are set in the dummy {@link HttpURLConnection}
	 */
	private void assertHeaderBody() {
		if (null==dummyConn) {
			try {
				dummyConn = new DummyHttpURLConnection(request.getURL());
				request.settleHttpHeaders();
				request.setConnectionProperties(dummyConn);
				request.outputBody(dummyConn);
			} catch (MalformedURLException ignored) {
			} catch (ProtocolException ignored) {
			} catch (IOException ignored) {
			} catch (HttpException ignored) {
			}
		}
	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		assertHeaderBody();
		if (null != dummyConn.outputData)
			return dummyConn.outputData.toByteArray();
		return null;
	}

	@Override
	public String getBodyContentType() {
		assertHeaderBody();
		return dummyConn.getRequestProperty(HTTP.CONTENT_TYPE);
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		assertHeaderBody();
		Map<String, List<String>> properties = dummyConn.getRequestProperties();
		Map<String, String> h = new HashMap<String, String>(properties.size());
		for (Entry<String, List<String>> prop : properties.entrySet()) {
			for (String value : prop.getValue()) {
				h.put(prop.getKey(), value);
			}
		}
		return h;
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		String parsed;
		try {
			parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
		} catch (UnsupportedEncodingException e) {
			parsed = new String(response.data);
		}
		return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
	}

	@Override
	protected void deliverResponse(String response) {
		if (null!=requestListener)
			requestListener.onResponse(response);
	}
}