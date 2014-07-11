package com.levelup.http.signed.oauth1.internal;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.TypedHttpRequest;

import oauth.signpost.http.HttpResponse;

public class ResponseAdapter implements HttpResponse {

	private final InputStream inputStream;
	private final com.levelup.http.HttpResponse response;

	public ResponseAdapter(TypedHttpRequest<?> request, InputStream inputStream) {
		this.inputStream = inputStream;
		this.response = request.getResponse();
	}

	@Override
	public int getStatusCode() throws IOException {
		return response.getResponseCode();
	}

	@Override
	public String getReasonPhrase() throws Exception {
		return response.getResponseMessage();
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
