package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * HTTP parameters suitable to pass to {@link HttpRequestPost} 
 */
public interface HttpPostParameters extends HttpParameters {
	
	/**
	 * Set request properties on the request before it's established, like the content-type or content-length
	 * @param request The POST request to set the parameters on
	 */
	void settleHttpHeaders(HttpRequestPost request);
	
	/**
	 * Output stream to write the body of the POST query
	 * @param output Stream to write into
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	void writeBodyTo(OutputStream output) throws UnsupportedEncodingException, IOException;
}
