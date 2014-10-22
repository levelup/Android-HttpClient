package co.tophe;


import android.support.annotation.Nullable;

import co.tophe.log.LoggerTagged;

/**
 * Interface for HTTP requests to be passed to {@link TopheClient}
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
	 * Returns the {@link co.tophe.log.LoggerTagged} for this request or {@code null}
	 */
	@Nullable
	LoggerTagged getLogger();

	/**
	 * Returns the {@link HttpConfig} for this request or {@code null} 
	 */
	@Nullable
	HttpConfig getHttpConfig();

	/**
	 * Set the {@link HttpConfig} for this request or {@code null} 
	 */
	@Nullable
	void setHttpConfig(HttpConfig config);

	public static final String HEADER_ACCEPT = "Accept";
	public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
}
