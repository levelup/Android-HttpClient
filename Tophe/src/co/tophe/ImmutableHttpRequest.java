package co.tophe;

import android.support.annotation.Nullable;

/**
 * An immutable HTTP request with its  response (if there was one).
 *
 * @author Created by Steve Lhomme on 14/07/2014.
 */
public interface ImmutableHttpRequest {
	/**
	 * The HTTP request that cannot be modified.
	 */
	HttpRequestInfo getHttpRequest();

	/**
	 * The HTTP response corresponding to {@link #getHttpRequest()}
	 */
	@Nullable
	HttpResponse getHttpResponse();
}
