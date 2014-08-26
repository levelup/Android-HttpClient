package com.levelup.http;

import java.io.IOException;

/**
 * Created by robUx4 on 26/08/2014.
 */
public interface ErrorHandler {
	DataErrorException handleError(HttpResponse httpResponse, ImmutableHttpRequest request, Exception cause) throws IOException, ParserException;
}
