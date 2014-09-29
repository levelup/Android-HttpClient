package com.levelup.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Thrown when the server returns an HTTP error
 * <p>It contains an object corresponding the error body sent by the server.
 * This object is parsed using {@link ServerErrorHandler ServerErrorHandler}..</p>
 *
 * @author Created by robUx4 on 24/09/2014.
 */
public class ServerException extends HttpError {
	private final Object parsedError;
	private final int httpStatusCode;
	private final HttpResponse response;
	private final HttpRequestInfo request;

	protected ServerException(Builder builder) {
		this.parsedError = builder.parsedError;
		this.httpStatusCode = builder.getHttpStatusCode();
		this.response = builder.response;
		this.request = builder.request;
	}

	/**
	 * @return The error object parsed by {@link ServerErrorHandler ServerErrorHandler}.
	 * May be {@code null}
	 */
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

	public static class Builder {
		private final Object parsedError;
		private final HttpResponse response;
		private final HttpRequestInfo request;

		public Builder(ImmutableHttpRequest request, Object parsedError) {
			this.parsedError = parsedError;
			this.response = request.getHttpResponse();
			this.request = request.getHttpRequest();
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

		public ServerException build() {
			return new ServerException(this);
		}
	}
}
