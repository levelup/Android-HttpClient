package com.levelup.http.signpost;

import com.levelup.http.HttpRequest;
import com.levelup.http.HttpRequestPost;

/**
 * Base {@link ProviderHttpRequestFactory} class that just creates a {@link HttpRequestPost} for each token retrieval request
 * 
 * @author Steve Lhomme
 */
public class BaseProviderHttpRequestFactory implements ProviderHttpRequestFactory {

	public BaseProviderHttpRequestFactory() {
	}

	public HttpRequest createRequest(String endpointUrl) {
		return new HttpRequestPost(endpointUrl, null);
	}
}
