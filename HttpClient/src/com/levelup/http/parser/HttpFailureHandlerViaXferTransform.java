package com.levelup.http.parser;

import java.io.IOException;

import com.levelup.http.HttpFailureException;
import com.levelup.http.HttpFailureHandler;
import com.levelup.http.HttpResponse;
import com.levelup.http.ImmutableHttpRequest;

/**
 * Created by robUx4 on 26/08/2014.
 */
public class HttpFailureHandlerViaXferTransform<T> implements HttpFailureHandler {

	public final XferTransform<HttpResponse, T> errorDataParser;

	public HttpFailureHandlerViaXferTransform(XferTransform<HttpResponse, T> errorDataParser) {
		this.errorDataParser = errorDataParser;
	}

	public HttpFailureException handleErrorData(T errorData, ImmutableHttpRequest request) throws IOException, ParserException {
		return new HttpFailureException.Builder(request, errorData).build();
	}

	@Override
	public final HttpFailureException getHttpFailureException(ImmutableHttpRequest request) throws IOException, ParserException {
		T errorData = errorDataParser.transformData(request.getHttpResponse(), request);
		return handleErrorData(errorData, request);
	}
}
