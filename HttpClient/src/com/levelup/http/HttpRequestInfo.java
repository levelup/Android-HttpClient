package com.levelup.http;

import android.net.Uri;

/**
 * Created by Steve Lhomme on 14/07/2014.
 */
public interface HttpRequestInfo {

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
	 * Get the single value of a HTTP header for this request
	 * @param name Name of the header
	 * @return The value of the header or {@code null} if it is not set
	 */
	String getHeader(String name);

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

	/**
	 * Get the object that will be responsible for signing the HTTP request
	 */
	RequestSigner getRequestSigner();
}
