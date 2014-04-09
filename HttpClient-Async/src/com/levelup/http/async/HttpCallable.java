package com.levelup.http.async;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpRequest;
import com.levelup.http.InputStreamParser;

class HttpCallable<T> implements Callable<T>, Closeable {
	private final HttpRequest request;
	private final InputStreamParser<T> parser;

	public HttpCallable(HttpRequest request, InputStreamParser<T> parser) {
		if (null==request) throw new IllegalArgumentException();
		this.request = request;
		this.parser = parser;
	}

	@Override
	public T call() throws Exception {
		return HttpClient.parseRequest(request, parser);
	}

	@Override
	public void close() throws IOException {
		final HttpURLConnection connection = request.getResponse();
		if (null!=connection)
			connection.disconnect();
	}
}