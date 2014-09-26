package com.levelup.http.parser;

import java.io.IOException;

import com.levelup.http.HttpFailureHandler;
import com.levelup.http.HttpFailure;
import com.levelup.http.HttpFailureException;
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
		HttpFailure httpFailure = new HttpFailure(errorData);
		return new HttpFailureException.Builder(request.getHttpRequest(), request.getHttpResponse(), httpFailure)
				.build();
	}

	@Override
	public final HttpFailureException getHttpFailureException(HttpResponse httpResponse, ImmutableHttpRequest request) throws IOException, ParserException {
		T errorData = errorDataParser.transformData(httpResponse, request);
		return handleErrorData(errorData, request);
	}
}
