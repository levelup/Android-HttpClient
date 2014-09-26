package com.levelup.http;

/**
 * Thrown when the server returns an HTTP error
 * <p>It contains an object corresponding the error body sent by the server.
 * This object is parsed using {@link HttpFailureHandler HttpFailureHandler}..</p>
 *
 * @author Created by robUx4 on 24/09/2014.
 */
public class HttpFailureException extends HttpException {
	private final Object parsedError;

	protected HttpFailureException(Builder builder) {
		super(builder);
		this.parsedError = builder.parsedError;
	}

	/**
	 * @return The error object parsed by {@link HttpFailureHandler HttpFailureHandler}.
	 * May be {@code null}
	 */
	public Object getParsedError() {
		return parsedError;
	}

	@Override
	public String toString() {
		return super.toString() + " parsedError:"+ parsedError;
	}

	public static class Builder extends HttpException.Builder {
		private final Object parsedError;

		public Builder(ImmutableHttpRequest request, Object parsedError) {
			super(request.getHttpRequest(), request.getHttpResponse());
			this.parsedError = parsedError;
		}

		@Override
		public HttpFailureException build() {
			return new HttpFailureException(this);
		}
	}
}
