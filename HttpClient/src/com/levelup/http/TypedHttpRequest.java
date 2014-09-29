package com.levelup.http;

/**
 * An {@link HttpRequest} that outputs data of type {@code T} 
 * @param <T> The type of data returned by the server
 */
public interface TypedHttpRequest<T, SE extends ServerException> extends HttpRequest {

	/**
	 * The {@link ResponseHandler} that will turn the response body into type {@code T}
	 * <p>MUST NOT BE {@code null} !
	 */
	ResponseHandler<T,SE> getResponseHandler();
	
}
