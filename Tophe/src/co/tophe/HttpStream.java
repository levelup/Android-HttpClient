package co.tophe;

import java.io.IOException;
import java.io.InputStream;

/**
 * Special output type for an "infinite" stream. It gives direct access to an {@link java.io.InputStream}. This type is not handled by the Ion engine for now.
 * <p>You must close the stream when you're finished with {@link #disconnect()}
 */
public class HttpStream {

	private final InputStream inputStream;
	private final ImmutableHttpRequest request;

	public HttpStream(InputStream inputStream, ImmutableHttpRequest request) throws IOException {
		if (null == inputStream) throw new IOException("we need an InputStream for the stream");
		this.inputStream = inputStream;
		this.request = request;
	}

	/**
	 * The {@link java.io.InputStream} where you can read the "live" data.
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Disconnect
	 */
	public void disconnect() {
		try {
			inputStream.close();
		} catch (IOException ignored) {
		} finally {
			request.getHttpResponse().disconnect();
		}
	}
}
