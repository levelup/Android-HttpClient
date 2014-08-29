package com.levelup.http.signed.oauth1.internal;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.BaseHttpRequest;

import oauth.signpost.http.HttpResponse;

public class OAuth1ResponseAdapter implements HttpResponse {

	private final InputStream inputStream;
    private final BaseHttpRequest<InputStream> httpRequest;

    public OAuth1ResponseAdapter(BaseHttpRequest<InputStream> engine, InputStream inputStream) {
		this.inputStream = inputStream;
		this.httpRequest = engine;
	}

    @Override
	public int getStatusCode() throws IOException {
		return httpRequest.getResponse().getResponseCode();
	}

	@Override
	public String getReasonPhrase() throws Exception {
		return httpRequest.getResponse().getResponseMessage();
	}

	@Override
	public InputStream getContent() {
		return inputStream;
	}

	@Override
	public BaseHttpRequest<InputStream> unwrap() {
		return httpRequest;
	}
}
