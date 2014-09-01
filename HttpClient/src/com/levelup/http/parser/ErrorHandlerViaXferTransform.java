package com.levelup.http.parser;

import java.io.IOException;

import com.levelup.http.DataErrorException;
import com.levelup.http.HttpResponse;
import com.levelup.http.ErrorHandler;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * Created by robUx4 on 26/08/2014.
 */
public class ErrorHandlerViaXferTransform<T> implements ErrorHandler {

	public final XferTransform<HttpResponse, T> errorDataParser;

	public ErrorHandlerViaXferTransform(XferTransform<HttpResponse, T> errorDataParser) {
		this.errorDataParser = errorDataParser;
	}

	public DataErrorException handleErrorData(T errorData, ImmutableHttpRequest request) throws IOException, ParserException {
		return new DataErrorException(errorData);
	}

	@Override
	public final DataErrorException handleError(HttpResponse httpResponse, ImmutableHttpRequest request) throws IOException, ParserException {
		T errorData = errorDataParser.transformData(httpResponse, request);
		return handleErrorData(errorData, request);
	}
}
