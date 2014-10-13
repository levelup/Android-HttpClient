package co.tophe;

import android.support.annotation.Nullable;

/**
 * Thrown when the server returns an HTTP error
 * <p>It contains an object corresponding to the parsed response body
 *
 * @author Created by robUx4 on 24/09/2014.
 */
public class ServerException extends TopheException {
	private final Object serverError;
	private final int httpStatusCode;
	private final HttpResponse response;
	private final HttpRequestInfo request;

	public ServerException(ImmutableHttpRequest request, Object serverError) {
		this.serverError = serverError;
		this.httpStatusCode = getHttpStatusCode(request.getHttpResponse());
		this.response = request.getHttpResponse();
		this.request = request.getHttpRequest();
	}

	/**
	 * @return The error object parsed by {@link ResponseHandler#errorParser}.
	 * May be {@code null}
	 */
	@Nullable
	public Object getServerError() {
		return serverError;
	}

	@Override
	public boolean isTemporaryFailure() {
		return httpStatusCode >= 500;
	}

	@Override
	public String getMessage() {
		return "serverError="+ String.valueOf(serverError);
	}

	@Override
	public int getStatusCode() {
		return httpStatusCode;
	}

	@Override
	public HttpRequestInfo getHttpRequest() {
		return request;
	}

	@Override
	public HttpResponse getHttpResponse() {
		return response;
	}

}
