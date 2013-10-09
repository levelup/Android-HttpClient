package com.levelup.http;

import java.net.HttpURLConnection;

public interface HttpExceptionCreator {

	/**
	 * Create a new {@link HttpException.Builder exception Builder} for this request
	 */
	HttpException.Builder newException();
	
	/**
	 * Create a new {@link HttpException.Builder exception Builder} for this request using data from the response
	 */
	HttpException.Builder newExceptionFromResponse(HttpURLConnection response);

}
