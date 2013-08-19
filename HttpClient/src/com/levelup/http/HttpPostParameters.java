package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

/**
 * HTTP parameters suitable to pass to {@link HttpRequestPost} 
 */
public interface HttpPostParameters extends HttpParameters {
	
	/**
	 * Set request properties on the connection before it's established, like the content-type or content-length
	 * @param connection Connection to configure
	 */
	void setRequestProperties(HttpURLConnection connection);
	
	/**
	 * Output stream to write the body of the POST query
	 * @param output Stream to write into
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	void writeBodyTo(OutputStream output) throws UnsupportedEncodingException, IOException;
}
