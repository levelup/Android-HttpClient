package co.tophe;

import android.support.annotation.NonNull;

/**
 * Error on the HTTP request before any network processing is involved
 */
public abstract class HttpRequestException extends HttpException {
	protected HttpRequestException(@NonNull AbstractBuilder builder) {
		super(builder);
	}

	public static abstract class AbstractBuilder<EXCEPTION extends HttpRequestException, BUILDER extends AbstractBuilder<EXCEPTION, ?>> extends HttpException.AbstractBuilder<EXCEPTION, BUILDER> {
		public AbstractBuilder(@NonNull HttpRequestInfo httpRequest) {
			super(httpRequest, null);
		}
	}
}
