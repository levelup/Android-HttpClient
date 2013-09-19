package com.levelup.http;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Exception that will occur by using {@link HttpClient}
 */
public class HttpException extends RuntimeException {

	// misc other errors returned by getErrorCode()
	static public final int ERROR_HTTP             = 4000;
	static public final int ERROR_TIMEOUT          = 4001;
	static public final int ERROR_NETWORK          = 4002;
	static public final int ERROR_HTTP_MIME        = 4003;
	static public final int ERROR_JSON             = 4004;
	static public final int ERROR_AUTH             = 4005;

	// HTTP errors found on getHttpStatusCode()
	static public final int ERROR_HTTP_BAD_REQUEST  = 400;
	static public final int ERROR_HTTP_UNAUTHORIZED = 401;
	static public final int ERROR_HTTP_FORBIDDEN    = 403;
	static public final int ERROR_HTTP_NOT_FOUND    = 404;
	static public final int ERROR_HTTP_GONE         = 410;
	static public final int ERROR_HTTP_BACKOFF      = 420; // Twitter thing
	static public final int ERROR_HTTP_RATELIMIT    = 429; // Twitter thing
	static public final int ERROR_HTTP_OVERLOADED   = 503;
	static public final int ERROR_HTTP_INTERNAL     = 506;


	private static final long serialVersionUID = 4993791558983072165L;
	
	private static final Header[] EMPTY_HEADERS = new Header[0]; 

	private final int mErrorCode;
	private final int mHttpStatusCode;
	private final HttpRequest httpRequest;
	protected final Header[] headers;

	protected HttpException(Builder builder) {
		super(builder.errorMessage, builder.exception);
		this.mErrorCode = builder.errorCode;
		this.mHttpStatusCode = builder.statusCode;
		this.httpRequest = builder.httpRequest;
		this.headers = builder.headers.toArray(EMPTY_HEADERS);
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append(getClass().getSimpleName());
		/*sb.append(' ');
		sb.append('#');
		sb.append(mErrorCode);
		if (mHttpStatusCode!=200) {
			sb.append(' ');
			sb.append("http:");
			sb.append(mHttpStatusCode);
		}
		sb.append(':');*/
		sb.append(getLocalizedMessage());
		if (null!=httpRequest) {
			sb.append(" on ");
			sb.append(httpRequest.getUri());
		}
		return sb.toString();
	}

	@Override
	public String getMessage() {
		final StringBuilder msg = new StringBuilder();
		if (0 != mErrorCode) {
			msg.append("#");
			msg.append(mErrorCode);
			msg.append(' ');
		}
		if (200 != mHttpStatusCode) {
			msg.append("http:");
			msg.append(mHttpStatusCode);
			msg.append(' ');
		}
		if (null!=httpRequest) {
			msg.append("req:");
			msg.append(httpRequest);
			msg.append(' ');
		}
		boolean hasMsg = false;
		if (null!=getCause()) {
			final String causeMsg = getCause().getMessage();
			if (!TextUtils.isEmpty(causeMsg)) {
				hasMsg = true;
				msg.append(causeMsg);
			}
		}
		if (!hasMsg) {
			final String superMsg = super.getMessage();
			if (!TextUtils.isEmpty(superMsg)) {
				msg.append(superMsg);
			}
		}
		return msg.toString();
	}

	public static class Builder {
		protected int errorCode = ERROR_HTTP;
		protected String errorMessage;
		protected Throwable exception;
		protected int statusCode;
		protected HttpRequest httpRequest;
		protected final List<Header> headers;

		public Builder(HttpRequest httpRequest) {
			this.httpRequest = httpRequest;
			Header[] srcHeaders = httpRequest.getAllHeaders();
			if (null==srcHeaders)
				this.headers = new ArrayList<Header>(0);
			else {
				this.headers = new ArrayList<Header>(srcHeaders.length);
				headers.addAll(Arrays.asList(srcHeaders));
			}
		}

		public Builder(HttpException e) {
			this.errorCode = e.getErrorCode();
			this.errorMessage = e.getMessage();
			this.exception = e.getCause();
			this.statusCode = e.mHttpStatusCode;
			this.httpRequest = e.httpRequest;
			this.headers = Arrays.asList(e.headers);
		}

		public Builder setErrorCode(int code) {
			this.errorCode = code;
			return this;
		}

		public Builder setErrorMessage(String message) {
			this.errorMessage = message;
			return this;
		}

		public Builder setCause(Throwable exception) {
			this.exception = exception;
			return this;
		}

		public Builder setHTTPResponse(HttpURLConnection resp) {
			if (null!=resp) {
				Map<String, List<String>> reqProperties = resp.getRequestProperties();
				headers.clear();
				for (Entry<String, List<String>> props : reqProperties.entrySet()) {
					for (String prop : props.getValue()) {
						headers.add(new Header(props.getKey(), prop));
					}
				}
				try {
					this.statusCode = resp.getResponseCode();
				} catch (IOException ignored) {
					this.statusCode = 200;
				}
			}
			return this;
		}

		public HttpException build() {
			HttpException result = new HttpException(this);
			return result;
		}
	}

	public static Builder fromFileNotFound(HttpExceptionCreator creator, HttpURLConnection resp, FileNotFoundException e) {
		InputStream errorStream = null;
		Builder builder = null;
		StringBuilder sb = null;
		try {
			builder = creator.newException();
			//builder.setCause(e);
			builder.setErrorCode(ERROR_HTTP);

			errorStream = resp.getErrorStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"), 1250);
			sb = new StringBuilder(resp.getContentLength() > 0 ? resp.getContentLength() : 64);
			String line;
			while ((line = reader.readLine())!=null) {
				if (!TextUtils.isEmpty(line)) {
					sb.append(line);
					sb.append('\n');
				}
			}

			if (creator instanceof AbstractHttpRequest && resp.getContentType()!=null && resp.getContentType().startsWith("application/json")) {
				JSONObject jsonData = new JSONObject(sb.toString());
				builder = ((AbstractHttpRequest) creator).handleJSONError(builder, jsonData);
			}

			builder.setErrorMessage(sb.toString());
		} catch (UnsupportedEncodingException ignored) {
		} catch (JSONException e1) {
			builder.setErrorMessage(sb.length()==0 ? "json error" : sb.toString());
			builder.setCause(e);
			builder.setErrorCode(ERROR_JSON);
			throw builder.build();
		} catch (IOException e1) {
		} finally {
			if (null!=errorStream) {
				try {
					errorStream.close();
				} catch (IOException e1) {
				}
				errorStream = null;
			}
		}
		return builder;
	}
}
