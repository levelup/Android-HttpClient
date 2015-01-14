package co.tophe;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import android.support.annotation.Nullable;

/**
 * Interface to define an HTTP response coming from any {@link HttpEngine}
 */
public interface HttpResponse {
	/**
	 * Get the content-type of the body received in the response. May be {@code null}.
	 */
	@Nullable
	String getContentType();

	/**
	 * Get the HTTP status code returned by the server.
	 * @throws IOException when reading from the response fails.
	 */
	int getResponseCode() throws IOException;

	/**
	 * Get all the headers returned by the server.
	 */
	Map<String, List<String>> getHeaderFields();

	/**
	 * Get the HTTP header field received from the server or {@code null} if that field doesn't exist.
	 */
	@Nullable
	String getHeaderField(String name);

	/**
	 * Get the {@code Content-Length} of the body or {@code -1} if this field is not set or cannot be represented as an
	 * {@code int}
	 */
	int getContentLength();

	/**
	 * Get the message in the HTTP status line.
	 * @throws IOException when reading from the response fails.
	 */
	String getResponseMessage() throws IOException;

	/**
	 * Get the {@code Content-Encoding} header value. Gzip and Inflate are handled transparently by TOPHE.
	 */
	@Nullable
	String getContentEncoding();

	/**
	 * Called to disconnect the response from the server when done reading the data from {@link #getContentStream()}.
	 * <p>For internal use only.</p>
	 */
	void disconnect();

	/**
	 * Get an {@link java.io.InputStream} access to the HTTP response body.
	 * <p>For internal use only.</p>
	 * @throws IOException when reading from the response fails.
	 */
	InputStream getContentStream() throws IOException;
}
