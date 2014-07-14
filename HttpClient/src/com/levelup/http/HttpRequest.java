package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Interface for HTTP requests to be passed to {@link HttpClient}
 * @see BaseHttpRequest
 */
public interface HttpRequest extends HttpRequestInfo, HttpExceptionCreator {

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
	 * Settle the HTTP headers for the lifetime of this request, useful to sign the query
	 */
	void settleHttpHeaders() throws HttpException;

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
	public <R extends HttpResponse> void setResponse(R resp);

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
}
