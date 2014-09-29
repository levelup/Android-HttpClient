package com.levelup.http;

import java.io.IOException;

import com.levelup.http.parser.ParserException;
import com.levelup.http.parser.XferTransform;

/**
 * Handle the body of the {@link com.levelup.http.HttpResponse} received from the {@link com.levelup.http.HttpEngine}
 * @param <OUTPUT> Type of the Object that the HTTP call will return
 * @param <SE> Type of the Exception that will be raised when the server reports an error
 * @author Created by robUx4 on 20/08/2014.
 */
public class ResponseHandler<OUTPUT, SE extends ServerException> {

	public final XferTransform<HttpResponse, OUTPUT> contentParser;
	public final ServerErrorHandler<?, SE> serverErrorHandler;

	/**
	 * {@link com.levelup.http.HttpResponse} handler, turns the HTTP body into a typed object/exception
	 * @param contentParser {@link com.levelup.http.parser.XferTransform} that will turn the body into an Object when there is no error
	 * @param serverErrorHandler {@link ServerErrorHandler} that will wrap the body in a {@link ServerException} when there is a server error
	 * @see BaseServerErrorHandler BaseServerErrorHandler for a common serverErrorHandler
	 */
	public ResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser, ServerErrorHandler<?, SE> serverErrorHandler) {
		if (null == contentParser) throw new NullPointerException("we need a parser for the content");
		if (null == serverErrorHandler) throw new NullPointerException("we need an error handler, consider BaseServerErrorHandler");
		this.contentParser = contentParser;
		this.serverErrorHandler = serverErrorHandler;
	}

	public <ERROR_DATA> ResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser, XferTransform<HttpResponse, ERROR_DATA> errorParser, final ServerExceptionFactory<ERROR_DATA, SE> serverExceptionFactory) {
		this(contentParser, new ServerErrorHandler<ERROR_DATA, SE>(errorParser) {
			@Override
			public SE exceptionFromErrorData(ERROR_DATA errorData, ImmutableHttpRequest request) throws IOException, ParserException {
				return serverExceptionFactory.createException(request, errorData);
			}
		});
	}

	public void onNewResponse(HttpResponse response, HttpRequest request) {
	}

	public Boolean followsRedirect() {
		return null;
	}
}
