package com.levelup.http;


public interface HttpExceptionFactory {

	/**
	 * Create a new {@link HttpException.Builder exception Builder} for this request
	 * @param response
	 */
	HttpException.Builder newException(HttpResponse response);
}
