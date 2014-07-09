package com.levelup.http.signed.oauth1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.Header;

import oauth.signpost.http.HttpRequest;

/**
 * Wrap a {@link HttpRequest HttpClient HttpRequest} to match the {@link oauth.signpost.http.HttpRequest signpost HttpRequest} interface
 */
public class OAuth1RequestAdapter implements HttpRequest {
	private final BaseHttpRequest<?> req;

	public OAuth1RequestAdapter(BaseHttpRequest<?> request) {
		this.req = request;
	}
	
	@Override
	public void setHeader(String name, String value) {
		req.setHeader(name, value);
	}
	
	@Override
	public String getHeader(String name) {
		return req.getHeader(name);
	}
	
	@Override
	public Map<String, String> getAllHeaders() {
		Header[] allHeaders = req.getAllHeaders();
		Map<String, String> result = new HashMap<String, String>(allHeaders.length);
		for (Header header : allHeaders) {
			result.put(header.getName(), header.getValue());
		}
		return result;
	}

	@Override
	public InputStream getMessagePayload() throws IOException {
		final String contentType = getContentType();  
		if (null != contentType && contentType.startsWith("application/x-www-form-urlencoded")) {
			ByteArrayOutputStream output = new ByteArrayOutputStream(32);
			req.outputBody(output);
			return new ByteArrayInputStream(output.toByteArray());
		}
		return null;
	}

	@Override
	public String getContentType() {
		return req.getContentType();
	}

	@Override
	public String getMethod() {
		return req.getHttpMethod();
	}

	@Override
	public String getRequestUrl() {
		return req.getUri().toString();
	}

	@Override
	public void setRequestUrl(String url) {
        // can't do
	}

	@Override
	public Object unwrap() {
		return req;
	}
}