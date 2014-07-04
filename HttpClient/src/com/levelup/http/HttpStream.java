package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.internal.OkDataCallback;

public class HttpStream {

	private final OkDataCallback inputStream;
	private final HttpRequest request;

	public HttpStream(OkDataCallback callback, HttpRequest request) {
		this.inputStream = callback;
		this.request = request;
	}

	public InputStream getInputStream() {
		return inputStream.getInputStream();
	}

	public void disconnect() {
		try {
			inputStream.close();
		} catch (IOException ignored) {
		} finally {
			//TODO request.getResponse().disconnect();
		}
	}

}
