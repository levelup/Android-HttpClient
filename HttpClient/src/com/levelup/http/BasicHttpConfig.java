package com.levelup.http;

import java.net.HttpURLConnection;


/**
 * Basic HTTP configuration to access servers
 */
public class BasicHttpConfig implements HttpConfig {

	private static final int READ_TIMEOUT_IN_MS = 6000; // 6s
	private static final int READ_TIMEOUT_LONG_POST_IN_MS = 80000; // 80s

	public static final BasicHttpConfig instance = new BasicHttpConfig();

	protected BasicHttpConfig() {}

	@Override
	public void configureTimeout(HttpURLConnection connection) {
		final String postType = connection.getRequestProperty("Content-Type");
		if (null!=postType && postType.startsWith("multipart/form-data"))
			connection.setReadTimeout(READ_TIMEOUT_LONG_POST_IN_MS);
		else
			connection.setReadTimeout(READ_TIMEOUT_IN_MS);
	}
}
