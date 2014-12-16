package co.tophe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
* I/O related exception, can occur when establishing the connection or when reading/writing data
*/
public class HttpIOException extends TopheNetworkException {
	protected HttpIOException(@NonNull Builder builder) {
		super(builder);
	}

	public static class Builder extends AbstractBuilder<HttpIOException, Builder> {

		public Builder(@NonNull HttpRequestInfo httpRequest, @Nullable HttpResponse response) {
			super(httpRequest, response);
		}

		@Override
		public HttpIOException build() {
			return new HttpIOException(this);
		}
	}
}
