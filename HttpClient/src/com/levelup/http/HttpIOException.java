package com.levelup.http;

/**
* Created by robUx4 on 24/09/2014.
*/
public class HttpIOException extends HttpException {
	HttpIOException(HttpException.Builder builder) {
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
		public HttpIOException build() {
			return new HttpIOException(this);
		}
	}
}
