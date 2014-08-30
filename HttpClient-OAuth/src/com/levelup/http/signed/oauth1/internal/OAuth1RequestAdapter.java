package com.levelup.http.signed.oauth1.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.levelup.http.Header;
import com.levelup.http.HttpEngine;

import oauth.signpost.http.HttpRequest;

/**
 * Wrap a {@link com.levelup.http.BaseHttpRequest HttpClient BaseHttpRequest} to match the {@link oauth.signpost.http.HttpRequest signpost HttpRequest} interface
 */
public class OAuth1RequestAdapter implements HttpRequest {
	private final HttpEngine<?> httpEngine;

    public OAuth1RequestAdapter(HttpEngine<?> httpEngine) {
		this.httpEngine = httpEngine;
	}

    @Override
	public void setHeader(String name, String value) {
        httpEngine.setHeader(name, value);
	}
	
	@Override
	public String getHeader(String name) {
		return httpEngine.getHttpRequest().getHeader(name);
	}
	
	@Override
	public Map<String, String> getAllHeaders() {
		Header[] allHeaders = httpEngine.getHttpRequest().getAllHeaders();
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
			httpEngine.getHttpRequest().getBodyParams().writeBodyTo(output, httpEngine.getHttpRequest(), null);
			return new ByteArrayInputStream(output.toByteArray());
		}
		return null;
	}

	@Override
	public String getContentType() {
		if (null!=httpEngine.getHttpRequest().getBodyParams())
			return httpEngine.getHttpRequest().getBodyParams().getContentType();
		return null;
	}

	@Override
	public String getMethod() {
		return httpEngine.getHttpRequest().getHttpMethod();
	}

	@Override
	public String getRequestUrl() {
		return httpEngine.getHttpRequest().getUri().toString();
	}

	@Override
	public void setRequestUrl(String url) {
        // can't do
	}

	@Override
	public HttpEngine<InputStream> unwrap() {
		return (HttpEngine<InputStream>) httpEngine;
	}
}