package com.levelup.http;


import com.levelup.http.log.LoggerTagged;

/**
 * Interface for HTTP requests to be passed to {@link HttpClient}
 * @see BaseHttpRequest
 */
public interface HttpRequest extends HttpRequestInfo {

	/**
	 * Add an extra HTTP header to this request
	 * @param name Name of the header
	 * @param value Value of the header
	 */
	void addHeader(String name, String value);

	/**
	 * Set an extra HTTP header to this request, removing all previous values
	 * @param name Name of the header
	 * @param value Value of the header
	 */
	void setHeader(String name, String value);

	/**
	 * Returns the {@link com.levelup.http.log.LoggerTagged} for this request or {@code null}
	 */
	LoggerTagged getLogger();

	/**
	 * Returns the {@link HttpConfig} for this request or {@code null} 
	 */
	HttpConfig getHttpConfig();

	/**
	 * Set the {@link HttpConfig} for this request or {@code null} 
	 */
	void setHttpConfig(HttpConfig config);

	public static final String HEADER_ACCEPT = "Accept";
	public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
}
