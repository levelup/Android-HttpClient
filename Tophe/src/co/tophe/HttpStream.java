package co.tophe;

import java.io.IOException;
import java.io.InputStream;

/**
 * Output type for an "infinite" stream
 */
public class HttpStream {

	private final InputStream inputStream;
	private final ImmutableHttpRequest request;

	public HttpStream(InputStream inputStream, ImmutableHttpRequest request) throws IOException {
		if (null==inputStream) throw new IOException("we need an InputStream for the stream");
		this.inputStream = inputStream;
		this.request = request;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void disconnect() {
		try {
			inputStream.close();
		} catch (IOException ignored) {
		} finally {
			request.getHttpResponse().disconnect();
		}
	}

}
