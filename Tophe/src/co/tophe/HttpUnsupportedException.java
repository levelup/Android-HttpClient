package co.tophe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
* Created by robUx4 on 24/09/2014.
*/
public class HttpUnsupportedException extends HttpException {
	HttpUnsupportedException(HttpException.Builder builder) {
		super(builder);
	}

	public static class Builder extends HttpException.Builder {

		public Builder(@NonNull HttpRequestInfo httpRequest, @Nullable HttpResponse response) {
			super(httpRequest, response);
		}

		public Builder(HttpException e) {
			super(e);
		}

		@Override
		public HttpUnsupportedException build() {
			return new HttpUnsupportedException(this);
		}
	}
}
