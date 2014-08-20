package com.levelup.http;

import com.levelup.http.parser.ResponseParser;

/**
 * An {@link HttpRequest} that outputs data of type {@code T} 
 * @param <T> The type of data returned by the server
 */
public interface TypedHttpRequest<T> extends HttpRequest {

	/**
	 * The {@link InputStreamParser} that will turn the response body into type {@code T}
	 * <p>MUST NOT BE {@code null} !
	 */
	ResponseParser<T, ?> getResponseParser();
	
}
