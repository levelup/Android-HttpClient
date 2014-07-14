package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;

import android.net.Uri;


/**
 * Interface for HTTP requests to be passed to {@link HttpClient}
 * @see BaseHttpRequest
 */
public interface HttpRequest extends HttpExceptionCreator {

	/**
	 * Get the target URL in {@link android.net.Uri Uri} format
	 */
	Uri getUri();

	/**
	 * Get the HTTP method used to process the request, like {@code "GET"} or {@code "POST"}
	 */
	String getHttpMethod();

	/**
	 * Get the content type of the body, or {@code null} if there's no body in the request
	 */
	String getContentType();

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
	 * Get the single value of a HTTP header for this request
	 * @param name Name of the header
	 * @return The value of the header or {@code null} if it is not set
	 */
	String getHeader(String name);

	/**
	 * Settle the HTTP headers for the lifetime of this request, useful to sign the query
	 * @param request
	 */
	void settleHttpHeaders(HttpRequest request) throws HttpException;

	/**
	 * Do the HTTP connection and send the request body if needed
	 * @throws IOException
	 */
	void doConnection() throws IOException;

	/**
	 * Output the HTTP body on the OutputStream
	 * @throws IOException
	 */
	void outputBody(OutputStream outputStream) throws IOException;

	/**
	 * Output the HTTP body on the connection
	 */
	void setupBody();

	/**
	 * Called when the request has been performed on the server, even if the response is an error
	 * @param resp Contains the received headers/data from the server
	 */

	void setResponse(HttpResponse resp);

	/**
	 * @return the HTTP response if there was any
	 */
	HttpResponse getResponse();

	/**
	 * Returns the {@link LoggerTagged} for this request or {@code null} 
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

	/**
	 * Get the list of extra headers set for this request
	 */
	Header[] getAllHeaders();

	/**
	 * Tell if the request has a body (for POST, PUT, DELETE, etc methods)
	 */
	boolean hasBody();

	/**
	 * Tell if the request is reading an object or just data from a continuous stream
	 */
	boolean isStreaming();
}
