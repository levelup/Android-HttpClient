package co.tophe.signed.oauth1.internal;

import java.io.IOException;
import java.io.InputStream;

import co.tophe.HttpEngine;
import co.tophe.ServerException;

import oauth.signpost.http.HttpResponse;

public class OAuth1ResponseAdapter implements HttpResponse {

	private final InputStream inputStream;
	private final HttpEngine<InputStream,ServerException> engine;

	public OAuth1ResponseAdapter(HttpEngine<InputStream,ServerException> engine, InputStream inputStream) {
		this.inputStream = inputStream;
		this.engine = engine;
	}

    @Override
	public int getStatusCode() throws IOException {
		return engine.getHttpResponse().getResponseCode();
	}

	@Override
	public String getReasonPhrase() throws Exception {
		return engine.getHttpResponse().getResponseMessage();
	}

	@Override
	public InputStream getContent() {
		return inputStream;
	}

	@Override
	public HttpEngine<InputStream,ServerException> unwrap() {
		return engine;
	}
}
