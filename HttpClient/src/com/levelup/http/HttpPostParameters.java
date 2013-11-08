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
	 * Set request properties on the request before it's established, like the content-type or content-length
	 * @param request The POST request to set the parameters on
	 */
	void settleHttpHeaders(HttpRequestPost request);
	
	/**
	 * Output stream to write the body of the POST query
	 * @param output Stream to write into
	 * @param request 
	 * @param progressListener TODO
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	void writeBodyTo(OutputStream output, HttpRequestPost request, UploadProgressListener progressListener) throws UnsupportedEncodingException, IOException;

	/**
	 * Set some extra properties on the {@link HttpURLConnection} before the connection is established
	 * @param connection to setup
	 */
	void setConnectionProperties(HttpURLConnection connection);
}
