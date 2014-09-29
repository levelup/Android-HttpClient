package com.levelup.http;

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
}
