package com.levelup.http;

import java.net.HttpURLConnection;

/**
 * HTTP config to connect to a web service
 */
public interface HttpConfig {

	/**
	 * Get the read timeout for the connection (may be null)
	 * @return read timeout in milliseconds, -1 for no read timeout
	 */
	int getReadTimeout(HttpURLConnection connection);
}
