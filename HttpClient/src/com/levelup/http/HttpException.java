package com.levelup.http;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;

/**
 * Exception that will occur by using {@link HttpClient}
 */
public class HttpException extends RuntimeException {

	// misc other errors
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

	private final int mErrorCode;
	private final int mHttpStatusCode;
	//private final HttpURLConnection httpResponse;
	private final String query;

	protected HttpException(int errorCode, HttpURLConnection httpResponse, String message, final StringBuilder query, Throwable tr) {
		super(message, tr);
		this.mErrorCode = errorCode;
		//this.httpResponse = httpResponse;
		int httpStatusCode = 200;
		if (null!=httpResponse)
			try {
				httpStatusCode = httpResponse.getResponseCode();
			} catch (IOException e) {
			}
		this.mHttpStatusCode = httpStatusCode;
		if (query!=null)
			this.query = query.toString();
		else
			this.query = null;
		if ((message==null || "null".equals(message)) && BuildConfig.DEBUG) throw new NullPointerException("We need an error message for "+mErrorCode+"/"+mHttpStatusCode+" query:"+query);
	}

	public int getErrorCode() {
		return mErrorCode;
	}

	public int getHttpStatusCode() {
		return mHttpStatusCode;
	}

	public String getQuery() {
		return query;
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
		if (!TextUtils.isEmpty(query)) {
			sb.append(" on ");
			sb.append(query);
		}
		return sb.toString();
	}

	@Override
	public String getMessage() {
		final StringBuilder msg = new StringBuilder();
		if (0 != mErrorCode) {
			msg.append("#");
			msg.append(String.valueOf(mErrorCode));
			msg.append(' ');
		}
		if (200 != mHttpStatusCode) {
			msg.append("http:");
			msg.append(String.valueOf(mHttpStatusCode));
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

	public boolean isUploadFailure() {
		return getErrorCode()==ERROR_NETWORK &&
				(getMessage().contains("ECONN") || getMessage().contains("server failed to respond"));
	}

	public static class Builder {
		protected int errorCode = ERROR_HTTP;
		protected String errorMessage;
		protected Throwable exception;
		protected HttpRequest httpRequest;
		protected HttpURLConnection httpResponse;

		/**
		 * see https://dev.twitter.com/docs/error-codes-responses
		 * @param code
		 * @return
		 */
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

		public Builder setHTTPRequest(HttpRequest request) {
			this.httpRequest = request;
			return this;
		}

		public Builder setHTTPResponse(HttpURLConnection resp) {
			this.httpResponse = resp;
			return this;
		}

		public HttpException build() {
			final StringBuilder fullMessage;
			if (httpRequest==null)
				fullMessage = new StringBuilder(0);
			else {
				fullMessage = new StringBuilder(httpRequest.toString());
				/* TODO if (httpRequest instanceof HttpPost) {
					HttpPost post = (HttpPost) httpRequest;
					HttpEntity entity = post.getEntity();
					if (entity instanceof UrlEncodedFormEntity) {
						UrlEncodedFormEntity ue = (UrlEncodedFormEntity) entity;
						if (ue.getContentLength() < 2000) {
							try {
								BufferedReader reader = new BufferedReader(new InputStreamReader(ue.getContent()));
								fullMessage.append(" params:").append(reader.readLine());
							} catch (IOException e) {
							}
						}
					}
				}*/
			}
			HttpException result = new HttpException(errorCode, httpResponse, errorMessage, fullMessage, exception);
			return result;
		}
	}

	public static Builder fromFileNotFound(HttpRequest request, HttpURLConnection resp, FileNotFoundException e) {
		InputStream errorStream = null;
		Builder builder = null;
		StringBuilder sb = null;
		try {
			builder = request.newException();
			//builder.setCause(e);
			builder.setErrorCode(ERROR_HTTP);
			builder.setHTTPResponse(resp);

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

			if (resp.getContentType()!=null && resp.getContentType().startsWith("application/json")) {
				JSONObject jsonData = new JSONObject(new JSONTokener(sb.toString()));
				builder = request.handleJSONError(builder, jsonData);
			}

			builder.setErrorMessage(sb.toString());
		} catch (UnsupportedEncodingException e1) {
		} catch (JSONException e1) {
			builder.setErrorMessage(sb.length()==0 ? "json error" : sb.toString());
			builder.setCause(e);
			builder.setErrorCode(ERROR_JSON);
			builder.setHTTPResponse(resp);
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
