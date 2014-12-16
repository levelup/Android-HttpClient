package co.tophe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
* Exception raised when there is a timeout connecting, reading or writing data on the HTTP connection
*/
public class HttpTimeoutException extends TopheNetworkException {
	protected HttpTimeoutException(@NonNull Builder builder) {
		super(builder);
	}

	public static class Builder extends AbstractBuilder<HttpTimeoutException, Builder> {
		public Builder(@NonNull HttpRequestInfo httpRequest, @Nullable HttpResponse response) {
			super(httpRequest, response);
		}

		@Override
		public HttpTimeoutException build() {
			return new HttpTimeoutException(this);
		}
	}
}
