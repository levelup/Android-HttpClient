package com.levelup.http.signed.oauth1.internal;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.BaseHttpRequest;

import oauth.signpost.http.HttpResponse;

public class ResponseAdapter implements HttpResponse {

	private final InputStream inputStream;
	private final com.levelup.http.HttpResponse headers;

	public ResponseAdapter(BaseHttpRequest<?> request, InputStream inputStream) {
		this.inputStream = inputStream;
		this.headers = request.getResponse();
	}

	@Override
	public int getStatusCode() throws IOException {
		return headers.getResponseCode();
	}

	@Override
	public String getReasonPhrase() throws Exception {
		return headers.getResponseMessage();
	}

	@Override
	public InputStream getContent() throws IOException {
		return inputStream;
	}

	@Override
	public Object unwrap() {
		return inputStream;
	}
}
