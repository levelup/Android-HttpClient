package com.levelup.http.parser;

import java.io.IOException;

import com.levelup.http.ErrorBody;
import com.levelup.http.ErrorHandler;
import com.levelup.http.HttpErrorBodyException;
import com.levelup.http.HttpResponse;
import com.levelup.http.ImmutableHttpRequest;

/**
 * Created by robUx4 on 26/08/2014.
 */
public class ErrorHandlerViaXferTransform<T> implements ErrorHandler {

	public final XferTransform<HttpResponse, T> errorDataParser;

	public ErrorHandlerViaXferTransform(XferTransform<HttpResponse, T> errorDataParser) {
		this.errorDataParser = errorDataParser;
	}

	public HttpErrorBodyException handleErrorData(T errorData, ImmutableHttpRequest request) throws IOException, ParserException {
		ErrorBody errorBody = new ErrorBody(errorData);
		return new HttpErrorBodyException.Builder(request.getHttpRequest(), request.getHttpResponse(), errorBody)
				.build();
	}

	@Override
	public final HttpErrorBodyException getHttpErrBodyException(HttpResponse httpResponse, ImmutableHttpRequest request) throws IOException, ParserException {
		T errorData = errorDataParser.transformData(httpResponse, request);
		return handleErrorData(errorData, request);
	}
}
