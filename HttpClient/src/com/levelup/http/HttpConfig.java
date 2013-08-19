package com.levelup.http;

import java.net.HttpURLConnection;

/**
 * HTTP config to connect to a web service
 */
public interface HttpConfig {

	/**
	 * Configure the {@link connection} with the proper timeout values
	 */
	void configureTimeout(HttpURLConnection connection);
	
}
