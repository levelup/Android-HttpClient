package com.levelup.http;

/**
 * Indicates there was a HTTP error,
 * <p>contains the error data parsed by {@link com.levelup.http.ErrorHandler#getHttpErrBodyException(HttpResponse, ImmutableHttpRequest) ErrorHandler.getHttpErrBodyException()}
 * </p>
 *
 * @author Created by robUx4 on 24/09/2014.
 */
public class HttpErrorBodyException extends HttpException {
	private final ErrorBody errorBody;

	protected HttpErrorBodyException(Builder builder) {
		super(builder);
		this.errorBody = builder.errorData;
	}

	/**
	 * @return The error object parsed by {@link com.levelup.http.ErrorHandler#getHttpErrBodyException(HttpResponse, ImmutableHttpRequest) ErrorHandler.getHttpErrBodyException()}.
	 * May be {@code null}
	 */
	public ErrorBody getErrorBody() {
		return errorBody;
	}

	@Override
	public String toString() {
		return super.toString() + " errorBody:"+errorBody;
	}

	public static class Builder extends HttpException.Builder {
		private ErrorBody errorData;

		public Builder(HttpRequestInfo httpRequest, HttpResponse response, ErrorBody errorData) {
			super(httpRequest, response);
			this.errorData = errorData;
		}

		public Builder(HttpException e) {
			super(e);
		}

		@Override
		public HttpErrorBodyException build() {
			return new HttpErrorBodyException(this);
		}
	}
}
