package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * HTTP parameters suitable to pass to {@link BaseHttpRequest} 
 */
public interface HttpBodyParameters extends HttpParameters {
	
	/**
	 * Set request properties on the request before it's established, like the content-type or content-length
	 * @param request The POST request to set the parameters on
	 */
	void settleHttpHeaders(BaseHttpRequest<?> request);
	
	/**
	 * Output stream to write the body of the POST query
	 * @param output Stream to write into
	 * @param request 
	 * @param progressListener TODO
	 * @throws IOException
	 */
	void writeBodyTo(OutputStream output, BaseHttpRequest<?> request, UploadProgressListener progressListener) throws IOException;

	/**
	 * Set some extra properties on the {@link HttpURLConnection} before the connection is established
	 * @param connection to setup
	 */
	void setConnectionProperties(HttpURLConnection connection);
}
