package com.levelup.http.async;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpRequest;
import com.levelup.http.HttpResponse;
import com.levelup.http.ResponseHandler;
import com.levelup.http.TypedHttpRequest;

/**
 * Base class to execute an {@link HttpRequest} and parse the received data to return the result of type {@code <T>}
 * @author Steve Lhomme
 *
 * @param <T>
 */
public class HttpCallable<T> implements Callable<T>, Closeable {
	private final TypedHttpRequest<T> request;
	private final ResponseHandler<T> parser;

	public HttpCallable(TypedHttpRequest<T> request) {
		if (null==request) throw new IllegalArgumentException();
		this.request = request;
		this.parser = request.getResponseHandler();
	}

	@Override
	public T call() throws Exception {
		return HttpClient.parseRequest(request);
	}

	@Override
	public void close() throws IOException {
		HttpResponse connection = request.getResponse();
		if (null!=connection)
			connection.disconnect();
	}
}