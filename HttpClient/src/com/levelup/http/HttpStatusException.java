package com.levelup.http;

/**
 * Indicates there was a HTTP error, the {@link #getCause()} must be a {@link com.levelup.http.DataErrorException DataErrorException}
* Created by robUx4 on 24/09/2014.
*/
public class HttpStatusException extends HttpException {
	HttpStatusException(HttpException.Builder builder) {
		super(builder);
	}

	public static class Builder extends HttpException.Builder {

		public Builder(HttpRequestInfo httpRequest, HttpResponse response) {
			super(httpRequest, response);
		}

		public Builder(HttpException e) {
			super(e);
		}

		@Override
		public HttpStatusException build() {
			return new HttpStatusException(this);
		}
	}
}
