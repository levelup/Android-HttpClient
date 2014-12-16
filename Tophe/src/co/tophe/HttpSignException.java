package co.tophe;

import android.support.annotation.NonNull;

/**
* Exception generated when we fail to generate a valid signature for the Request
*/
public class HttpSignException extends HttpException {
	HttpSignException(HttpException.Builder builder) {
		super(builder);
	}

	public static class Builder extends HttpException.Builder {

		public Builder(@NonNull HttpRequestInfo httpRequest) {
			super(httpRequest, null);
		}

		@Override
		public HttpSignException build() {
			return new HttpSignException(this);
		}
	}
}
