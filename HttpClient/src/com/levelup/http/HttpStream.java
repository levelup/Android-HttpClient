package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.internal.BlockingDataCallback;

public class HttpStream {

	private final BlockingDataCallback dataBuffer;

	public HttpStream(BlockingDataCallback callback) {
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
