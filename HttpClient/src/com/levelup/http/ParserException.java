package com.levelup.http;

/**
 * Runtime Exception that occurs when data parsing with {@link InputStreamParser} fails
 */
public class ParserException extends RuntimeException {
	private static final long serialVersionUID = 3213822444086259097L;
	private final String sourceData;

	public ParserException(String detailMessage, Exception cause, String sourceData) {
		super(detailMessage, cause);
		this.sourceData = sourceData;
	}

	/**
	 * Get the source data that created the parsing error
	 * @return {@code null} if the data was not available
	 */
	public String getSourceData() {
		return sourceData;
	}
}