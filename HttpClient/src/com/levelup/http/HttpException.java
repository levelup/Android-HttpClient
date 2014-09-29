package com.levelup.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.text.TextUtils;

/**
 * Exception that will occur by using {@link HttpClient}
 */
public class HttpException extends HttpError {

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

	private final HttpRequestInfo request;
	private final HttpResponse response;
	private final int httpStatusCode;

	protected HttpException(Builder builder) {
		super(builder.errorMessage, builder.exception);
		this.httpStatusCode = getHttpStatusCode(builder.response);
		this.request = builder.httpRequest;
		this.response = builder.response;
	}

	/**
	 * @return whether this error was caused by a network or server issue
	 */
	@Override
	public boolean isTemporaryFailure() {
		return (this instanceof HttpIOException
				|| this instanceof HttpTimeoutException
				|| this instanceof HttpMimeException
				|| httpStatusCode >= 500);
	}

	@Override
	public int getStatusCode() {
		return httpStatusCode;
	}

	@Override
	public HttpRequestInfo getHttpRequest() {
		return request;
	}

	@Override
	public HttpResponse getHttpResponse() {
		return response;
	}

	@Override
	public List<Header> getReceivedHeaders() {
		if (null!=response) {
			try {
				final Map<String, List<String>> responseHeaders = response.getHeaderFields();
				if (null != responseHeaders) {
					ArrayList<Header> receivedHeaders = new ArrayList<Header>(responseHeaders.size());
					for (Entry<String, List<String>> entry : responseHeaders.entrySet()) {
						for (String value : entry.getValue()) {
							receivedHeaders.add(new Header(entry.getKey(), value));
						}
					}
					return receivedHeaders;
				}
			} catch (IllegalStateException ignored) {
				// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/689
			} catch (IllegalArgumentException e) {
				// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/875
			} catch (NullPointerException e) {
				// issue https://github.com/square/okhttp/issues/348
			}
		}
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		/*sb.append(' ');
		sb.append('#');
		sb.append(errorCode);
		if (httpStatusCode!=200) {
			sb.append(' ');
			sb.append("http:");
			sb.append(httpStatusCode);
		}
		sb.append(':');*/
		/*if (null!=request) {
			sb.append(" on ");
			sb.append(request);
		}*/
		return getClass().getSimpleName() + ' ' + getLocalizedMessage();
	}

	@Override
	public String getMessage() {
		final StringBuilder msg = new StringBuilder();
		if (0 != httpStatusCode) {
			msg.append("http:");
			msg.append(httpStatusCode);
			msg.append(' ');
		}
		if (null!= request) {
			msg.append("req:");
			msg.append(request);
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

		public Builder(HttpRequestInfo httpRequest, HttpResponse response) {
			if (null==httpRequest) throw new NullPointerException("a HttpException needs a request");
			this.httpRequest = httpRequest;
			this.response = response;
		}

		public Builder(HttpException e) {
			this.errorMessage = e.getMessage();
			this.exception = e.getCause();
			this.httpRequest = e.request;
			this.response = e.response;
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
