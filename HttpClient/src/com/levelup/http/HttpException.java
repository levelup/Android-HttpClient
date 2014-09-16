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
public class HttpException extends Exception {

	static public final int ERROR_DEFAULT          = 4000;
	static public final int ERROR_TIMEOUT          = 4001;
	static public final int ERROR_NETWORK          = 4002;
	static public final int ERROR_MIME             = 4003;
	static public final int ERROR_JSON             = 4004;
	static public final int ERROR_AUTH             = 4005;
	/**	Indicates there was a data parsing error, the {@link #getCause()} must be a {@link com.levelup.http.parser.ParserException ParserException} */
	static public final int ERROR_PARSER           = 4006;
	/**	Indicates there was a HTTP error, the {@link #getCause()} must be a {@link com.levelup.http.DataErrorException DataErrorException} */
	static public final int ERROR_DATA_MSG         = 4007;
	public static final int ERROR_ENGINE           = 4008;

	static public final int ERROR_HTTP_BAD_REQUEST  = 400;
	static public final int ERROR_HTTP_UNAUTHORIZED = 401;
	static public final int ERROR_HTTP_FORBIDDEN    = 403;
	static public final int ERROR_HTTP_NOT_FOUND    = 404;
	static public final int ERROR_HTTP_NOT_ACCEPTABLE = 406;
	static public final int ERROR_HTTP_GONE         = 410;
	static public final int ERROR_HTTP_TOO_LONG     = 413;
	static public final int ERROR_HTTP_BAD_RANGE    = 416;
	static public final int ERROR_HTTP_BACKOFF      = 420; // Twitter thing
	static public final int ERROR_HTTP_RATELIMIT    = 429; // Twitter thing
	static public final int ERROR_HTTP_OVERLOADED   = 503;
	static public final int ERROR_HTTP_GATEWAY_TIMEOUT = 504;
	static public final int ERROR_HTTP_INTERNAL     = 506;


	private static final long serialVersionUID = 4993791558983072165L;

	/**
	 * The {@link com.levelup.http.HttpRequestInfo} that generated this Exception
	 */
	public final HttpRequestInfo request;

	/**
	 * The {@link com.levelup.http.HttpResponse} that generated this Exception, may be {@code null}
	 */
	public final HttpResponse response;

	/**
	 * {@link com.levelup.http.HttpException} internal error code
	 * @see #ERROR_DATA_MSG
	 * @see #ERROR_AUTH
	 * @see #ERROR_TIMEOUT
	 * @see #ERROR_NETWORK
	 * @see #ERROR_MIME
	 */
	public final int errorCode;

	/**
	 * The HTTP status code sent by the server for this Rxception
	 * <p>see <a href="https://dev.twitter.com/docs/error-codes-responses">Twitter website</a> for some special cases</p>
	 * <p>0 if we didn't receive any HTTP response for this Exception</p>
	 */
	public final int httpStatusCode;

	protected HttpException(Builder builder) {
		super(builder.errorMessage, builder.exception);
		this.errorCode = builder.errorCode;
		this.httpStatusCode = builder.getHttpStatusCode();
		this.request = builder.httpRequest;
		this.response = builder.response;
	}

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
		if (0 != errorCode) {
			msg.append("#");
			msg.append(errorCode);
			msg.append(' ');
		}
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
		protected int errorCode = ERROR_DEFAULT;
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
			this.errorCode = e.errorCode;
			this.errorMessage = e.getMessage();
			this.exception = e.getCause();
			this.httpRequest = e.request;
			this.response = e.response;
		}

		public Builder setErrorCode(int code) {
			this.errorCode = code;
			return this;
		}

		public int getErrorCode() {
			return errorCode;
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

		/**
		 * Get the HTTP status code for this Request exception
		 * <p>see <a href="https://dev.twitter.com/docs/error-codes-responses">Twitter website</a> for some special cases</p>
		 * @return 0 if we didn't receive any HTTP response
		 */
		public int getHttpStatusCode() {
			if (null!= response) {
				try {
					return response.getResponseCode();
				} catch (IllegalStateException e) {
					// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/689
				} catch (NullPointerException ignored) {
					// okhttp 2.0 bug https://github.com/square/okhttp/issues/348
				} catch (IOException e) {
				}
			}
			return 0;
		}

		public HttpException build() {
			return new HttpException(this);
		}
	}
}
