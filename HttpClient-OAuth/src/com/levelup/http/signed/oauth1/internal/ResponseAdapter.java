package com.levelup.http.signed.oauth1.internal;

import java.io.IOException;
import java.io.InputStream;

import com.koushikdutta.async.http.libcore.RawHeaders;
import com.levelup.http.BaseHttpRequest;

import oauth.signpost.http.HttpResponse;

public class ResponseAdapter implements HttpResponse {

	private final InputStream inputStream;
	private final RawHeaders headers;

	public ResponseAdapter(BaseHttpRequest<?> request, InputStream inputStream) {
		this.inputStream = inputStream;
		this.headers = request.getResponse().getHeaders();
	}

	@Override
	public int getStatusCode() throws IOException {
		return headers.getResponseCode();
	}

	@Override
	public String getReasonPhrase() throws Exception {
		return headers.getStatusLine();
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
