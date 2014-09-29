package com.levelup.http;

import java.io.IOException;

import com.levelup.http.parser.ParserException;
import com.levelup.http.parser.XferTransform;

/**
 * Handle the HTTP response body when the server returned an error
 * @author Created by robUx4 on 26/08/2014.
 */
public abstract class ServerErrorHandler<T, SE extends ServerException> {

	public final XferTransform<HttpResponse, T> errorDataParser;

	public ServerErrorHandler(XferTransform<HttpResponse, T> errorDataParser) {
		this.errorDataParser = errorDataParser;
	}

	public abstract SE exceptionFromErrorData(T errorData, ImmutableHttpRequest request) throws IOException, ParserException;

	/**
	 * Parse the server error {@link com.levelup.http.HttpResponse}
	 * @param request The request that created the server error
	 * @return The {@link ServerException} containing the parsed server error data. NOT {@code null}
	 * @throws IOException
	 * @throws ParserException
	 */
	public final SE getServerErrorException(ImmutableHttpRequest request) throws IOException, ParserException {
		T errorData = errorDataParser.transformData(request.getHttpResponse(), request);
		return exceptionFromErrorData(errorData, request);
	}
}
