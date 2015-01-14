package co.tophe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Exception raised when the MIME type of the HTTP response doesn't match the one in the {@code Accept} field, usually when a proxy returns an error.
 */
public class HttpMimeException extends TopheNetworkException {
	protected HttpMimeException(@NonNull Builder builder) {
		super(builder);
	}

	public static class Builder extends AbstractBuilder {

		public Builder(@NonNull HttpRequestInfo httpRequest, @Nullable HttpResponse response) {
			super(httpRequest, response);
		}

		@Override
		public HttpMimeException build() {
			return new HttpMimeException(this);
		}
	}
}
