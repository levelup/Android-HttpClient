package com.levelup.http;

/**
 * Indicates there was a data parsing error, the {@link #getCause()} must be a {@link com.levelup.http.parser.ParserException ParserException}
* Created by robUx4 on 24/09/2014.
*/
public class HttpDataParserException extends HttpException {
	HttpDataParserException(HttpException.Builder builder) {
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
		public HttpDataParserException build() {
			return new HttpDataParserException(this);
		}
	}
}
