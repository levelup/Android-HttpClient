package co.tophe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.tophe.parser.ParserException;

/**
 * Indicates there was a data parsing error, the {@link #getCause()} is a {@link co.tophe.parser.ParserException ParserException}
 * <p>Either because the parser is not adequate for the server data or because a proxy returned error data
 *
 * @author Created by robUx4 on 24/09/2014.
 */
public class HttpDataParserException extends HttpException {
	protected HttpDataParserException(@NonNull AbstractBuilder builder) {
		super(builder);
	}

	@Override
	public ParserException getCause() {
		return (ParserException) super.getCause();
	}

	public static class Builder extends AbstractBuilder<HttpDataParserException, Builder> {

		public Builder(@NonNull HttpRequestInfo httpRequest, @Nullable HttpResponse response, @NonNull ParserException e) {
			super(httpRequest, response);
			super.setCause(e);
		}

		@Override
		public Builder setCause(Throwable cause) {
			throw new IllegalStateException("pass the parser exception in the constructor");
		}

		@Override
		public HttpDataParserException build() {
			return new HttpDataParserException(this);
		}
	}
}
