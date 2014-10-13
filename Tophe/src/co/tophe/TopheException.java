package co.tophe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.support.annotation.Nullable;

/**
 * Base exception to catch {@link co.tophe.HttpException} and {@link co.tophe.ServerException} as the same time
 * @author Created by robUx4 on 29/09/2014.
 */
public abstract class TopheException extends Exception {

	protected TopheException() {
	}

	protected TopheException(String errorMessage, Throwable exception) {
		super(errorMessage, exception);
	}

	/**
	 * The HTTP status code sent by the server for this Exception
	 * <p>see <a href="https://dev.twitter.com/docs/error-codes-responses">Twitter website</a> for some special cases</p>
	 * <p>0 if we didn't receive any HTTP response for this Exception</p>
	 */
	public abstract int getStatusCode();

	/**
	 * The {@link HttpRequestInfo} that generated this Exception
	 */
	public abstract HttpRequestInfo getHttpRequest();

	/**
	 * The {@link HttpResponse} that generated this Exception, may be {@code null}
	 */
	@Nullable
	public abstract HttpResponse getHttpResponse();

	public abstract boolean isTemporaryFailure();

	public List<Header> getReceivedHeaders() {
		if (null!=getHttpResponse()) {
			try {
				final Map<String, List<String>> responseHeaders = getHttpResponse().getHeaderFields();
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
