package com.levelup.http;

public interface TypedHttpRequest<T> extends HttpRequest {

	InputStreamParser<T> getInputStreamParser();
	
}
