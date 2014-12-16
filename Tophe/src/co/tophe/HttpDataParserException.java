package co.tophe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.tophe.parser.ParserException;

/**
 * Indicates there was a data parsing error, the {@link #getCause()} is a {@link co.tophe.parser.ParserException ParserException}
 *
 * @author Created by robUx4 on 24/09/2014.
 */
public class HttpDataParserException extends HttpException {
	HttpDataParserException(HttpException.Builder builder) {
		super(builder);
	}

	@Override
	public ParserException getCause() {
		return (ParserException) super.getCause();
	}

	public static class Builder extends HttpException.Builder {

		public Builder(@NonNull HttpRequestInfo httpRequest, @Nullable HttpResponse response, ParserException e) {
			super(httpRequest, response);
			super.setCause(e);
		}

		public Builder(HttpException e) {
			super(e);
		}

		@Override
		public HttpException.Builder setCause(Throwable tr) {
			throw new IllegalStateException("pass the parser exception in the constructor");
		}

		@Override
		public HttpDataParserException build() {
			return new HttpDataParserException(this);
		}
	}
}
