package com.levelup.http;


public interface HttpExceptionCreator {

	/**
	 * Create a new {@link HttpException.Builder exception Builder} for this request
	 */
	HttpException.Builder newException();
	
	/**
	 * Create a new {@link HttpException.Builder exception Builder} for this request using data from the response
	 * @param cause to create this exception, may be {@code null}
	 */
	HttpException.Builder newExceptionFromResponse(Throwable cause);

}
