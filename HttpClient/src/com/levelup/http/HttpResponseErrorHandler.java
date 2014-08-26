package com.levelup.http;

/**
 * Created by robUx4 on 26/08/2014.
 */
public interface HttpResponseErrorHandler {
	DataErrorException handleError(HttpResponse httpResponse, ImmutableHttpRequest request, Exception cause);
}
