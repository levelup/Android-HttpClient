package com.levelup.http;

import java.io.IOException;

import com.levelup.http.parser.ParserException;
import com.levelup.http.parser.XferTransform;

/**
 * Handle the HTTP response body when the server returned an error
 * @author Created by robUx4 on 26/08/2014.
 */
public class HttpFailureHandler<T> {

	public final XferTransform<HttpResponse, T> errorDataParser;

	public HttpFailureHandler(XferTransform<HttpResponse, T> errorDataParser) {
		this.errorDataParser = errorDataParser;
	}

	public HttpFailureException exceptionFromErrorData(T errorData, ImmutableHttpRequest request) throws IOException, ParserException {
		return new HttpFailureException.Builder(request, errorData).build();
	}

	/**
	 * Parse the server error {@link com.levelup.http.HttpResponse}
	 * @param request The request that created the server error
	 * @return The {@link HttpFailureException} containing the parsed server error data
	 * @throws IOException
	 * @throws ParserException
	 */
	public final HttpFailureException getHttpFailureException(ImmutableHttpRequest request) throws IOException, ParserException {
		T errorData = errorDataParser.transformData(request.getHttpResponse(), request);
		return exceptionFromErrorData(errorData, request);
	}
}
