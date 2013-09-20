package com.levelup.http;



/**
 * Basic HTTP configuration to access servers
 */
public class BasicHttpConfig implements HttpConfig {

	private static final int READ_TIMEOUT_IN_MS = 6000; // 6s
	private static final int READ_TIMEOUT_LONG_POST_IN_MS = 80000; // 80s

	public static final BasicHttpConfig instance = new BasicHttpConfig();

	protected BasicHttpConfig() {}

	@Override
	public int getReadTimeout(HttpRequest request) {
		if (null!=request) {
			final String postType = request.getHeader("Content-Type");
			if (null!=postType && postType.startsWith("multipart/form-data"))
				return READ_TIMEOUT_LONG_POST_IN_MS;
		}

		return READ_TIMEOUT_IN_MS;
	}
}
