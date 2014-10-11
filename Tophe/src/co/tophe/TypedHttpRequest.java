package co.tophe;

import android.support.annotation.NonNull;

/**
 * An {@link HttpRequest} that outputs data of type {@code T} 
 * @param <T> The type of data returned by the server
 */
public interface TypedHttpRequest<T, SE extends ServerException> extends HttpRequest {

	/**
	 * The {@link ResponseHandler} that will turn the response body into type {@code T}
	 * <p>MUST NOT BE {@code null} !
	 */
	@NonNull
	ResponseHandler<T,SE> getResponseHandler();
	
}
