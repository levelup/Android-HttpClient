package com.levelup.http;

import java.io.IOException;

import com.levelup.http.parser.ParserException;

/**
 * Created by robUx4 on 29/09/2014.
 */
public abstract class ServerExceptionFactory<ERROR_DATA, SE extends ServerException> {
	public abstract SE createException(ImmutableHttpRequest request, ERROR_DATA errorData) throws IOException, ParserException;
}
