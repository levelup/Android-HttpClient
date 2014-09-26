package com.levelup.http;

import java.io.IOException;

import com.levelup.http.parser.ParserException;

/**
 * Handle the HTTP response body when the server returned an error
 * @author Created by robUx4 on 26/08/2014.
 */
public interface HttpFailureHandler {
	/**
	 * Parse the {@link com.levelup.http.HttpResponse} to extract an {@link HttpFailure}
	 * @param request The request that created the server error
	 * @return The {@link HttpFailureException} containing the parsed {@link HttpFailure}
	 * @throws IOException
	 * @throws ParserException
	 */
	HttpFailureException getHttpFailureException(ImmutableHttpRequest request) throws IOException, ParserException;
}
