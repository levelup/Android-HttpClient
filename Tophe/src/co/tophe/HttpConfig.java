package co.tophe;


/**
 * Configuration for an HTTP request. Only the read-timeout tweaking can be configured for now.
 *
 * @see co.tophe.BasicHttpConfig
 */
public interface HttpConfig {

	/**
	 * Get the read timeout for the request (may be null)
	 *
	 * @param request the HTTP request to configure.
	 * @return read timeout in milliseconds, -1 for no read timeout
	 */
	int getReadTimeout(HttpRequestInfo request);
}
