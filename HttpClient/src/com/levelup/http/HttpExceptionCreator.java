package com.levelup.http;


public interface HttpExceptionCreator {

	/**
	 * Create a new {@link HttpException.Builder exception Builder} for this request
	 */
	HttpException.Builder newException();
}
