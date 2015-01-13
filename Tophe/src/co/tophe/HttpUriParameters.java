package co.tophe;

import android.net.Uri;

/**
 * HTTP parameters suitable to pass to {@link RawHttpRequest.AbstractBuilder#setUrl(String, HttpUriParameters) RawHttpRequest.Builder.setUrl()}
 *
 * @see co.tophe.UriParams
 */
public interface HttpUriParameters extends HttpParameters {

	/**
	 * Add the parameters for this HTTP query in the URL
	 *
	 * @param uriBuilder {@link Uri.Builder Builder} used to build the HTTP query
	 */
	void appendUriParameters(Uri.Builder uriBuilder);

}
