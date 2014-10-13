package co.tophe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Exception that will occur by using {@link HttpClient}
 */
public class HttpException extends TopheException {

	static public final int HTTP_STATUS_BAD_REQUEST     = 400;
	static public final int HTTP_STATUS_UNAUTHORIZED    = 401;
	static public final int HTTP_STATUS_FORBIDDEN       = 403;
	static public final int HTTP_STATUS_NOT_FOUND       = 404;
	static public final int HTTP_STATUS_NOT_ACCEPTABLE  = 406;
	static public final int HTTP_STATUS_GONE            = 410;
	static public final int HTTP_STATUS_TOO_LONG        = 413;
	static public final int HTTP_STATUS_BAD_RANGE       = 416;
	static public final int HTTP_STATUS_BACKOFF         = 420; // Twitter thing
	static public final int HTTP_STATUS_RATELIMIT       = 429; // Twitter thing
	static public final int HTTP_STATUS_SERVER_ERROR    = 500;
	static public final int HTTP_STATUS_OVERLOADED      = 503;
	static public final int HTTP_STATUS_GATEWAY_TIMEOUT = 504;
	static public final int HTTP_STATUS_INTERNAL        = 506;


	private static final long serialVersionUID = 4993791558983072165L;

	protected HttpException(Builder builder) {
		super(builder.httpRequest, builder.response, builder.errorMessage);
		initCause(builder.exception);
	}

	/**
	 * @return whether this error was caused by a network or server issue
	 */
	@Override
	public boolean isTemporaryFailure() {
		return (this instanceof HttpIOException
				|| this instanceof HttpTimeoutException
				|| this instanceof HttpMimeException
				|| super.isTemporaryFailure());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ' ' + getLocalizedMessage();
	}

	@Override
	public String getMessage() {
		final StringBuilder msg = new StringBuilder();
		if (0 != getStatusCode()) {
			msg.append("http:");
			msg.append(getStatusCode());
			msg.append(' ');
		}
		if (null!= getHttpRequest()) {
			msg.append("req:");
			msg.append(getHttpRequest());
			msg.append(' ');
		}
		/*boolean hasMsg = false;
		if (null!=getCause()) {
			final String causeMsg = getCause().getMessage();
			if (!TextUtils.isEmpty(causeMsg)) {
				hasMsg = true;
				msg.append(causeMsg);
			}
		}*/
		final String superMsg = super.getMessage();
		if (!TextUtils.isEmpty(superMsg)) {
			//if (hasMsg) msg.append(' ');
			msg.append(superMsg);
		}
		return msg.toString();
	}

	public static class Builder {
		protected String errorMessage;
		protected Throwable exception;
		protected final HttpRequestInfo httpRequest;
		protected final HttpResponse response;

		public Builder(@NonNull HttpRequestInfo httpRequest,@Nullable HttpResponse response) {
			if (null==httpRequest) throw new NullPointerException("a HttpException needs a request");
			this.httpRequest = httpRequest;
			this.response = response;
		}

		public Builder(@NonNull HttpException e) {
			this.errorMessage = e.getMessage();
			this.exception = e.getCause();
			this.httpRequest = e.getHttpRequest();
			this.response = e.getHttpResponse();
		}

		public Builder setErrorMessage(String message) {
			this.errorMessage = message;
			return this;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public Builder setCause(Throwable tr) {
			this.exception = tr;
			return this;
		}

		public Throwable getCause() {
			return exception;
		}

		public HttpException build() {
			return new HttpException(this);
		}
	}
}
