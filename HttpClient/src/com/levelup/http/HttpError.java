package com.levelup.http;

import java.io.IOException;
import java.util.List;

/**
 * Created by robUx4 on 29/09/2014.
 */
public abstract class HttpError extends Exception {

	protected HttpError() {
	}

	protected HttpError(String errorMessage, Throwable exception) {
		super(errorMessage, exception);
	}

	/**
	 * The HTTP status code sent by the server for this Exception
	 * <p>see <a href="https://dev.twitter.com/docs/error-codes-responses">Twitter website</a> for some special cases</p>
	 * <p>0 if we didn't receive any HTTP response for this Exception</p>
	 */
	public abstract int getStatusCode();

	/**
	 * The {@link com.levelup.http.HttpRequestInfo} that generated this Exception
	 */
	public abstract HttpRequestInfo getHttpRequest();

	/**
	 * The {@link com.levelup.http.HttpResponse} that generated this Exception, may be {@code null}
	 */
	public abstract HttpResponse getHttpResponse();

	public abstract boolean isTemporaryFailure();

	public abstract List<Header> getReceivedHeaders();

	/**
	 * Get the HTTP status code for this Request exception
	 * <p>see <a href="https://dev.twitter.com/docs/error-codes-responses">Twitter website</a> for some special cases</p>
	 * @return 0 if we didn't receive any HTTP response
	 */
	protected static int getHttpStatusCode(HttpResponse response) {
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
}
