package co.tophe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Thrown when the server returns an HTTP error
 * <p>It contains an object corresponding to the parsed response body
 *
 * @author Created by robUx4 on 24/09/2014.
 */
public class ServerException extends TopheException {
	private final Object serverError;

	public ServerException(@NonNull ImmutableHttpRequest request, @Nullable Object serverError) {
		super(request.getHttpRequest(), request.getHttpResponse(), "serverError="+ String.valueOf(serverError));
		this.serverError = serverError;
	}

	/**
	 * @return The error object parsed by {@link ResponseHandler#errorParser}.
	 * May be {@code null}
	 */
	@Nullable
	public Object getServerError() {
		return serverError;
	}
}
