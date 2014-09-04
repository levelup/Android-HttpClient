package com.levelup.http;

import java.io.IOException;

import com.levelup.http.parser.ParserException;

/**
 * Created by robUx4 on 26/08/2014.
 */
public interface ErrorHandler {
	DataErrorException handleError(HttpResponse httpResponse, ImmutableHttpRequest request) throws IOException, ParserException;
}
