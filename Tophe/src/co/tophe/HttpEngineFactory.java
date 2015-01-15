package co.tophe;

import android.support.annotation.Nullable;

/**
 * Interface to create an {@link co.tophe.HttpEngine} based on the Builder.
 *
 * @author Created by Steve Lhomme on 15/07/2014.
 */
public interface HttpEngineFactory {

	/**
	 * Create an {@link co.tophe.HttpEngine} to process the HTTP request set in the builder.
	 *
	 * @param builder contains all the information needed to build and process the HTTP request for this builder.
	 * @param <T>     type of data returned by the engine after parsing the HTTP response body.
	 * @param <SE>    type of exception raised when a server-generated error is returned in the response.
	 * @return the engine to process the HTTP request or {@code null} if the request cannot be handled.
	 */
	@Nullable
	<T, SE extends ServerException> HttpEngine<T, SE> createEngine(HttpEngine.Builder<T, SE> builder);

}
