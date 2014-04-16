package com.levelup.http.signpost;

import com.levelup.http.HttpRequest;

/**
 * Interface used by {@link HttpClientOAuthProvider} to create HTTP requests to retrieve OAuth tokens
 * <p>By default {@link BaseProviderHttpRequestFactory} is used
 * 
 * @author Steve Lhomme
 */
public interface ProviderHttpRequestFactory {
	
	/**
	 * Create an HTTP request for the given endpoint
	 */
	HttpRequest createRequest(String endpointUrl);
}
