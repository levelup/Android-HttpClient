package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

public class HttpStream {

	private final InputStream inputStream;
	private final HttpRequest request;

	public HttpStream(InputStream inputStream, HttpRequest request) throws IOException {
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
			request.getResponse().disconnect();
		}
	}

}
