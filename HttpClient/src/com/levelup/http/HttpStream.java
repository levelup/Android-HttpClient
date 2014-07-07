package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.internal.OkDataCallback;

public class HttpStream {

	private final OkDataCallback dataBuffer;
	private final HttpRequest request;

	public HttpStream(OkDataCallback callback, HttpRequest request) {
		this.dataBuffer = callback;
		this.request = request;
	}

	public InputStream getInputStream() {
		return dataBuffer.getInputStream();
	}

	public void disconnect() {
		try {
			dataBuffer.close();
		} catch (IOException ignored) {
		} finally {
			//TODO request.getResponse().disconnect();
		}
	}

}
