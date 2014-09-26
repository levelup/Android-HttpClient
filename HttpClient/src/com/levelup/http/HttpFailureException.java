package com.levelup.http;

/**
 * Thrown when the server returns an HTTP error
 * <p>It contains an {@link com.levelup.http.HttpFailure} corresponding the error body sent by the server.
 * This object is parsed using {@link HttpFailureHandler#getHttpFailureException(ImmutableHttpRequest) HttpFailureHandler.getHttpFailureException()}</p>
 *
 * @author Created by robUx4 on 24/09/2014.
 */
public class HttpFailureException extends HttpException {
	private final HttpFailure httpFailure;

	protected HttpFailureException(Builder builder) {
		super(builder);
		this.httpFailure = builder.errorData;
	}

	/**
	 * @return The error object parsed by {@link HttpFailureHandler#getHttpFailureException(ImmutableHttpRequest) HttpFailureHandler.getHttpFailureException()}.
	 * May be {@code null}
	 */
	public HttpFailure getHttpFailure() {
		return httpFailure;
	}

	@Override
	public String toString() {
		return super.toString() + " httpFailure:"+ httpFailure;
	}

	public static class Builder extends HttpException.Builder {
		private final HttpFailure errorData;

		public Builder(ImmutableHttpRequest request, HttpFailure errorData) {
			super(request.getHttpRequest(), request.getHttpResponse());
			this.errorData = errorData;
		}

		@Override
		public HttpFailureException build() {
			return new HttpFailureException(this);
		}
	}
}
