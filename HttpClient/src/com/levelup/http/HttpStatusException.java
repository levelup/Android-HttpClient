package com.levelup.http;

/**
 * Indicates there was a HTTP error, the {@link #getCause()} is a {@link com.levelup.http.DataErrorException DataErrorException}
 *
 * @author Created by robUx4 on 24/09/2014.
 */
public class HttpStatusException extends HttpException {
	HttpStatusException(HttpException.Builder builder) {
		super(builder);
	}

	@Override
	public DataErrorException getCause() {
		return (DataErrorException) super.getCause();
	}

	public static class Builder extends HttpException.Builder {

		public Builder(HttpRequestInfo httpRequest, HttpResponse response, DataErrorException cause) {
			super(httpRequest, response);
			super.setCause(cause);
		}

		public Builder(HttpException e) {
			super(e);
		}

		@Override
		public HttpException.Builder setCause(Throwable tr) {
			throw new IllegalStateException("pass the parser exception in the constructor");
		}

		@Override
		public HttpStatusException build() {
			return new HttpStatusException(this);
		}
	}
}
