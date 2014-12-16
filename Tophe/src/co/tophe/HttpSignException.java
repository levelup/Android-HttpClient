package co.tophe;

import android.support.annotation.NonNull;

/**
 * Exception generated when we fail to generate a valid signature for the Request
 */
public class HttpSignException extends HttpRequestException {
	protected HttpSignException(@NonNull Builder builder) {
		super(builder);
	}

	public static class Builder extends AbstractBuilder<HttpSignException, Builder> {

		public Builder(@NonNull HttpRequestInfo httpRequest) {
			super(httpRequest);
		}

		public HttpSignException build() {
			return new HttpSignException(this);
		}
	}
}
