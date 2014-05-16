package com.levelup.http;

/**
 * Runtime Exception that occurs when data parsing with {@link InputStreamParser} fails
 */
public class ParserException extends RuntimeException {
	private static final long serialVersionUID = 3213822444086259097L;

	public ParserException(Exception e) {
		super(e);
	}

	public ParserException(String detailMessage, Exception e) {
		super(detailMessage, e);
	}
}