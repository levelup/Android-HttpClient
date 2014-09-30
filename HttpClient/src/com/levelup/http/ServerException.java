package com.levelup.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.support.annotation.Nullable;

/**
 * Thrown when the server returns an HTTP error
 * <p>It contains an object corresponding the error body sent by the server.
 *
 * @author Created by robUx4 on 24/09/2014.
 */
public class ServerException extends HttpError {
	private final Object parsedError;
	private final int httpStatusCode;
	private final HttpResponse response;
	private final HttpRequestInfo request;

	public ServerException(ImmutableHttpRequest request, Object parsedError) {
		this.parsedError = parsedError;
		this.httpStatusCode = getHttpStatusCode(request.getHttpResponse());
		this.response = request.getHttpResponse();
		this.request = request.getHttpRequest();
	}

	/**
	 * @return The error object parsed by {@link com.levelup.http.ResponseHandler#errorParser}.
	 * May be {@code null}
	 */
	@Nullable
	public Object getParsedError() {
		return parsedError;
	}

	@Override
	public boolean isTemporaryFailure() {
		return httpStatusCode >= 500;
	}

	public List<Header> getReceivedHeaders() {
		if (null!=response) {
			try {
				final Map<String, List<String>> responseHeaders = response.getHeaderFields();
				if (null != responseHeaders) {
					ArrayList<Header> receivedHeaders = new ArrayList<Header>(responseHeaders.size());
					for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
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
		return super.toString() + " parsedError:"+ parsedError;
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

}
