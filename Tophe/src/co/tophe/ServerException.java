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

	public static final int HTTP_STATUS_BAD_REQUEST     = 400;
	public static final int HTTP_STATUS_UNAUTHORIZED    = 401;
	public static final int HTTP_STATUS_FORBIDDEN       = 403;
	public static final int HTTP_STATUS_NOT_FOUND       = 404;
	public static final int HTTP_STATUS_NOT_ACCEPTABLE  = 406;
	public static final int HTTP_STATUS_GONE            = 410;
	public static final int HTTP_STATUS_TOO_LONG        = 413;
	public static final int HTTP_STATUS_BAD_RANGE       = 416;
	public static final int HTTP_STATUS_BACKOFF         = 420; // Twitter thing
	public static final int HTTP_STATUS_RATELIMIT       = 429; // Twitter thing
	public static final int HTTP_STATUS_SERVER_ERROR    = 500;
	public static final int HTTP_STATUS_OVERLOADED      = 503;
	public static final int HTTP_STATUS_GATEWAY_TIMEOUT = 504;
	public static final int HTTP_STATUS_INTERNAL        = 506;
	
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
