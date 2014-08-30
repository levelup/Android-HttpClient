package com.levelup.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Exception that will occur by using {@link HttpClient}
 */
public class HttpException extends Exception {

	// misc other errors returned by getErrorCode()
	static public final int ERROR_HTTP             = 4000;
	static public final int ERROR_TIMEOUT          = 4001;
	static public final int ERROR_NETWORK          = 4002;
	static public final int ERROR_HTTP_MIME        = 4003;
	static public final int ERROR_JSON             = 4004;
	static public final int ERROR_AUTH             = 4005;
	/**	Indicates there was a data parsing error, the {@link #getCause()} must be a {@link com.levelup.http.ParserException ParserException} */
	static public final int ERROR_PARSER           = 4006;
	/**	Indicates there was a HTTP error, the {@link #getCause()} must be a {@link com.levelup.http.DataErrorException DataErrorException} */
	static public final int ERROR_DATA_MSG         = 4007;

	// HTTP errors found on getHttpStatusCode()
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

	private final int mErrorCode;
	private final int mHttpStatusCode;
	private final HttpRequest httpRequest;
	private final Header[] headers;
	private final Header[] receivedHeaders;

	protected HttpException(Builder builder) {
		super(builder.errorMessage, builder.exception);
		this.mErrorCode = builder.errorCode;
		this.mHttpStatusCode = builder.statusCode;
		this.httpRequest = builder.httpRequest;
		this.headers = builder.headers.toArray(new Header[builder.headers.size()]);
		this.receivedHeaders = builder.receivedHeaders.toArray(new Header[builder.receivedHeaders.size()]);
		if ((getMessage()==null || "null".equals(getMessage())) && BuildConfig.DEBUG) throw new NullPointerException("We need an error message for "+mErrorCode+"/"+mHttpStatusCode+" query:"+httpRequest);
	}

	/**
	 * Get the internal type of error
	 */
	public int getErrorCode() {
		return mErrorCode;
	}

	/**
	 * Get the HTTP status code for this Request exception
	 * <p>see <a href="https://dev.twitter.com/docs/error-codes-responses">Twitter website</a> for some special cases</p>
	 */
	public int getHttpStatusCode() {
		return mHttpStatusCode;
	}

	/**
	 * Get the {@link android.net.Uri Uri} corresponding to the query, may be {@code null}
	 */
	public Uri getUri() {
		if (null==httpRequest)
			return null;
		return httpRequest.getUri();
	}

	/**
	 * Get the full {@link HttpRequest} that triggered the exception
	 * @return
	 */
	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	public Header[] getAllHeaders() {
		return headers;
	}

	public Header[] getReceivedHeaders() {
		return receivedHeaders;
	}

	@Override
	public String toString() {
		/*sb.append(' ');
		sb.append('#');
		sb.append(mErrorCode);
		if (mHttpStatusCode!=200) {
			sb.append(' ');
			sb.append("http:");
			sb.append(mHttpStatusCode);
		}
		sb.append(':');*/
		/*if (null!=httpRequest) {
			sb.append(" on ");
			sb.append(httpRequest);
		}*/
		return getClass().getSimpleName() + ' ' + getLocalizedMessage();
	}

	@Override
	public String getMessage() {
		final StringBuilder msg = new StringBuilder();
		if (0 != mErrorCode) {
			msg.append("#");
			msg.append(mErrorCode);
			msg.append(' ');
		}
		if (0 != mHttpStatusCode) {
			msg.append("http:");
			msg.append(mHttpStatusCode);
			msg.append(' ');
		}
		if (null!=httpRequest) {
			msg.append("req:");
			msg.append(httpRequest);
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
		protected int errorCode = ERROR_HTTP;
		protected String errorMessage;
		protected Throwable exception;
		protected int statusCode;
		protected final HttpRequest httpRequest;
		protected final List<Header> headers;
		protected List<Header> receivedHeaders;

		public Builder(HttpRequest httpRequest, HttpResponse response) {
			this.httpRequest = httpRequest;
			Header[] srcHeaders = httpRequest.getAllHeaders();
			if (null==srcHeaders)
				this.headers = Collections.emptyList();
			else {
				this.headers = new ArrayList<Header>(srcHeaders.length);
				headers.addAll(Arrays.asList(srcHeaders));
			}

			if (null == response) {
				this.receivedHeaders = Collections.emptyList();
			} else {
				setHTTPResponse(response);
				try {
					final Map<String, List<String>> responseHeaders = response.getHeaderFields();
					if (null == responseHeaders)
						this.receivedHeaders = Collections.emptyList();
					else {
						this.receivedHeaders = new ArrayList<Header>(responseHeaders.size());
						for (Entry<String, List<String>> entry : responseHeaders.entrySet()) {
							for (String value : entry.getValue()) {
								receivedHeaders.add(new Header(entry.getKey(), value));
							}
						}
					}
				} catch (IllegalStateException ignored) {
					// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/689
					this.receivedHeaders = Collections.emptyList();
				} catch (IllegalArgumentException e) {
					// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/875
					this.receivedHeaders = Collections.emptyList();
				} catch (NullPointerException e) {
					// issue https://github.com/square/okhttp/issues/348
					this.receivedHeaders = Collections.emptyList();
				}
			}
		}

		public Builder(HttpException e) {
			this.errorCode = e.getErrorCode();
			this.errorMessage = e.getMessage();
			this.exception = e.getCause();
			this.statusCode = e.mHttpStatusCode;
			this.httpRequest = e.httpRequest;
			this.headers = Arrays.asList(e.headers);
			this.receivedHeaders = Arrays.asList(e.receivedHeaders);
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
		 * Alternative to {@link #setHTTPResponse(HttpResponse)} to simulate some issues
		 * @param statusCode
		 * @return The builder for easy chaining
		 */
		public Builder setHttpStatusCode(int statusCode) {
			this.statusCode = statusCode;
			return this;
		}

		public Builder setHTTPResponse(HttpResponse resp) {
			if (null!=resp) {
				try {
					this.statusCode = resp.getResponseCode();
				} catch (IllegalStateException e) {
					// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/689
					this.statusCode = 200;
				} catch (NullPointerException ignored) {
					// okhttp 2.0 bug https://github.com/square/okhttp/issues/348
					this.statusCode = 200;
				} catch (IOException e) {
					this.statusCode = 200;
				}
			}
			return this;
		}

		public HttpException build() {
			return new HttpException(this);
		}
	}
}
