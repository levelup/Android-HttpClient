package com.levelup.http;

import java.io.IOException;

import com.levelup.http.parser.ParserException;

/**
 * Created by robUx4 on 26/08/2014.
 */
public interface ErrorHandler {
	/**
	 * Parse the {@link com.levelup.http.HttpResponse} to extract an {@link com.levelup.http.ErrorBody}
	 * @param httpResponse The server error response
	 * @param request The request that created the server error
	 * @return The {@link com.levelup.http.HttpErrorBodyException} containing the parsed {@link com.levelup.http.ErrorBody}
	 * @throws IOException
	 * @throws ParserException
	 */
	HttpErrorBodyException getHttpErrBodyException(HttpResponse httpResponse, ImmutableHttpRequest request) throws IOException, ParserException;
}
