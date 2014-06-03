package com.levelup.http;

import android.net.Uri;

/**
 * HTTP parameters suitable to pass to {@link BaseHttpRequest.Builder#setUrl(String, HttpUriParameters) BaseHttpRequest.Builder.setUrl()} 
 */
public interface HttpUriParameters extends HttpParameters {

	/**
	 * Add the parameters for this HTTP GET query in the URL
	 * @param uriBuilder {@link Uri.Builder Builder} used to build the HTTP GET query
	 */
	void addUriParameters(Uri.Builder uriBuilder);

}
