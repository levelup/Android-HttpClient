package co.tophe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Base exception to catch {@link co.tophe.ServerException} and {@link co.tophe.HttpException} at the same time.
 *
 * @author Created by robUx4 on 29/09/2014.
 */
public abstract class TopheException extends Exception {
	private final int httpStatusCode;
	private final HttpResponse response;
	private final HttpRequestInfo request;

	/**
	 * Constructor.
	 *
	 * @param request       the HTTP request that generated the exception.
	 * @param response      the HTTP response there was one.
	 * @param detailMessage the detail message for this exception.
	 */
	protected TopheException(@NonNull HttpRequestInfo request, @Nullable HttpResponse response, @Nullable String detailMessage) {
		super(detailMessage);
		this.request = request;
		this.response = response;
		this.httpStatusCode = getHttpStatusCode(response);
	}

	/**
	 * The HTTP status code sent by the server for this Exception
	 * <p>see <a href="https://dev.twitter.com/docs/error-codes-responses">Twitter website</a> for some special cases</p>
	 * <p>-1 if we didn't receive any HTTP response for this Exception or the status code could not be read</p>
	 */
	public int getStatusCode() {
		return httpStatusCode;
	}

	/**
	 * The {@link HttpRequestInfo} that generated this Exception.
	 */
	@NonNull
	public HttpRequestInfo getHttpRequest() {
		return request;
	}

	/**
	 * The {@link HttpResponse} that generated this Exception, may be {@code null}.
	 */
	@Nullable
	public HttpResponse getHttpResponse() {
		return response;
	}

	/**
	 * @return whether this error was caused by a network or server issue, but is not a logical server error. (eg status code 500
	 * on the server means the server didn't handle the request properly, so is a temporary error)
	 */
	public boolean isTemporaryFailure() {
		return httpStatusCode >= 500;
	}

	/**
	 * @return a list of all the HTTP headers received. An empty list when there is no response.
	 */
	public List<Header> getReceivedHeaders() {
		if (null != response) {
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
			} catch (IllegalArgumentException ignored) {
				// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/875
			} catch (NullPointerException ignored) {
				// issue https://github.com/square/okhttp/issues/348
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Get the HTTP status code for this Request exception
	 * <p>see <a href="https://dev.twitter.com/docs/error-codes-responses">Twitter website</a> for some special cases</p>
	 *
	 * @return 0 if we didn't receive any HTTP response
	 */
	private static int getHttpStatusCode(HttpResponse response) {
		if (null != response) {
			try {
				return response.getResponseCode();
			} catch (IllegalStateException ignored) {
				// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/689
			} catch (NullPointerException ignored) {
				// okhttp 2.0 bug https://github.com/square/okhttp/issues/348
			} catch (IOException ignored) {
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ' ' + getLocalizedMessage();
	}

	@Override
	public String getMessage() {
		final StringBuilder msg = new StringBuilder();
		if (0 != getStatusCode()) {
			msg.append("http:");
			msg.append(getStatusCode());
			msg.append(' ');
		}
		msg.append("req:");
		msg.append(getHttpRequest());
		msg.append(' ');
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
}
