package com.levelup.http;

/**
 * An {@link HttpRequest} that outputs data of type {@code T} 
 * @param <T> The type of data returned by the server
 */
public interface TypedHttpRequest<T> extends HttpRequest {

	/**
	 * The {@link HttpResponseHandler} that will turn the response body into type {@code T}
	 * <p>MUST NOT BE {@code null} !
	 */
	HttpResponseHandler<T> getResponseHandler();
	
}
