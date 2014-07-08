package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.internal.OkDataCallback;

public class HttpStream {

	private final OkDataCallback dataBuffer;

	public HttpStream(OkDataCallback callback) {
		this.dataBuffer = callback;
	}

	public InputStream getInputStream() {
		return dataBuffer.getInputStream();
	}

	public void disconnect() {
		try {
			dataBuffer.close();
		} catch (IOException ignored) {
		}
	}

}
