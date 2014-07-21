package com.levelup.http.signed.oauth1.internal;

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
 * Wrap a {@link BaseHttpRequest HttpClient BaseHttpRequest} to match the {@link oauth.signpost.http.HttpRequest signpost HttpRequest} interface
 */
public class OAuth1RequestAdapter implements HttpRequest {
	private final BaseHttpRequest<?> httpRequest;

    public OAuth1RequestAdapter(BaseHttpRequest<?> httpRequest) {
		this.httpRequest = httpRequest;
	}

    @Override
	public void setHeader(String name, String value) {
        httpRequest.setHeader(name, value);
	}
	
	@Override
	public String getHeader(String name) {
		return httpRequest.getHeader(name);
	}
	
	@Override
	public Map<String, String> getAllHeaders() {
		Header[] allHeaders = httpRequest.getAllHeaders();
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
            httpRequest.outputBody(output);
			return new ByteArrayInputStream(output.toByteArray());
		}
		return null;
	}

	@Override
	public String getContentType() {
		return httpRequest.getContentType();
	}

	@Override
	public String getMethod() {
		return httpRequest.getHttpMethod();
	}

	@Override
	public String getRequestUrl() {
		return httpRequest.getUri().toString();
	}

	@Override
	public void setRequestUrl(String url) {
        // can't do
	}

	@Override
	public BaseHttpRequest<?> unwrap() {
		return httpRequest;
	}
}