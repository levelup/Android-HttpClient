package com.levelup.http;


import android.text.format.DateUtils;

/**
 * Basic HTTP configuration to access servers
 */
public class BasicHttpConfig implements HttpConfig {

	private static final int READ_TIMEOUT_IN_MS = (int) (6 * DateUtils.SECOND_IN_MILLIS);
	private static final int READ_TIMEOUT_LONG_POST_IN_MS = (int) (80 * DateUtils.SECOND_IN_MILLIS);

	public static final BasicHttpConfig instance = new BasicHttpConfig();

	protected BasicHttpConfig() {}

	@Override
	public int getReadTimeout(HttpRequestInfo request) {
		if (null!=request) {
			final String postType = request.getHeader("Content-Type");
			if (null!=postType && postType.startsWith("multipart/form-data"))
				return READ_TIMEOUT_LONG_POST_IN_MS;
		}

		return READ_TIMEOUT_IN_MS;
	}
}
