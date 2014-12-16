package co.tophe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Network related exception like a timeout, IO error
 * @see HttpIOException
 * @see HttpTimeoutException
 * @see HttpMimeException
 */
public class TopheNetworkException extends HttpException {
	protected TopheNetworkException(@NonNull AbstractBuilder builder) {
		super(builder);
	}

	@Override
	public boolean isTemporaryFailure() {
		return true;
	}

	public static abstract class AbstractBuilder<EXCEPTION extends TopheNetworkException, BUILDER extends AbstractBuilder<EXCEPTION, ?>> extends HttpException.AbstractBuilder<EXCEPTION, BUILDER> {
		public AbstractBuilder(@NonNull HttpRequestInfo httpRequest, @Nullable HttpResponse response) {
			super(httpRequest, response);
		}
	}

	public static class Builder extends AbstractBuilder<TopheNetworkException, Builder> {
		public Builder(@NonNull HttpRequestInfo httpRequest, @Nullable HttpResponse response) {
			super(httpRequest, response);
		}

		@Override
		public TopheNetworkException build() {
			return new TopheNetworkException(this);
		}
	}
}
