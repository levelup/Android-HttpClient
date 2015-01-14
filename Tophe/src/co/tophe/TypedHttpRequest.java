package co.tophe;

import android.support.annotation.NonNull;

/**
 * An {@link HttpRequest} that outputs data of type {@code T}
 *
 * @param <T>  the type of data returned by the server
 * @param <SE> the type of the Exception raised for all server generated errors.
 * @see co.tophe.BaseHttpRequest
 */
public interface TypedHttpRequest<T, SE extends ServerException> extends HttpRequest {

	/**
	 * The {@link ResponseHandler} that will turn the response body into type {@link T} and also turn the server generated errors into type {@link SE}
	 */
	@NonNull
	ResponseHandler<T, SE> getResponseHandler();

}
