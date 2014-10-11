package co.tophe;

import android.net.Uri;
import android.support.annotation.Nullable;

import co.tophe.body.HttpBodyParameters;
import co.tophe.signed.RequestSigner;

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
	@Nullable
	HttpBodyParameters getBodyParameters();

	/**
	 * Get the single value of a HTTP header for this request
	 * @param name Name of the header
	 * @return The value of the header or {@code null} if it is not set
	 */
	@Nullable
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
