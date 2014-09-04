package com.levelup.http;

import android.net.Uri;

import com.levelup.http.body.HttpBodyParameters;
import com.levelup.http.signed.RequestSigner;

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
	 * Get the body for this request, or {@code null} if there's no body in the request
	 */
	HttpBodyParameters getBodyParameters();

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
	 * Get the object that will be responsible for signing the HTTP request
	 */
	RequestSigner getRequestSigner();
}
