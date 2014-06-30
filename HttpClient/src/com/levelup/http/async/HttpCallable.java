package com.levelup.http.async;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpRequest;
import com.levelup.http.InputStreamParser;
import com.levelup.http.TypedHttpRequest;

/**
 * Base class to execute an {@link HttpRequest} and parse the received data to return the result of type {@code <T>}
 * @author Steve Lhomme
 *
 * @param <T>
 */
public class HttpCallable<T> implements Callable<T>, Closeable {
	private final HttpRequest request;
	private final InputStreamParser<T> parser;

	public HttpCallable(TypedHttpRequest<T> request) {
		if (null==request) throw new IllegalArgumentException();
		this.request = request;
		this.parser = request.getInputStreamParser();
	}

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
		/** TODO not working the same in Ion
		final HttpURLConnection connection = request.getResponse();
		if (null!=connection)
			connection.disconnect();
			*/
	}
}