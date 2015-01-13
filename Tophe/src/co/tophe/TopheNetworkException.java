package co.tophe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A network related exception like a timeout, IO error. This is not an exception generated from a server error like {@link co.tophe.ServerException}
 *
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

	/**
	 * Builder for a {@link co.tophe.TopheNetworkException} child.
	 *
	 * @param <EXCEPTION> The class of the exception that will be raised.
	 * @param <BUILDER>   The build type that should be used to raise the {@link EXCEPTION}.
	 */
	public static abstract class AbstractBuilder<EXCEPTION extends TopheNetworkException, BUILDER extends AbstractBuilder<EXCEPTION, ?>> extends HttpException.AbstractBuilder<EXCEPTION, BUILDER> {
		public AbstractBuilder(@NonNull HttpRequestInfo httpRequest, @Nullable HttpResponse response) {
			super(httpRequest, response);
		}
	}

	/**
	 * Builder for a generic {@link co.tophe.TopheNetworkException}. (ie not HttpIOException, HttpTimeoutException or HttpMimeException)
	 */
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
