package com.levelup.http;

import com.levelup.http.parser.XferTransform;

/**
 * Handle the body of the {@link com.levelup.http.HttpResponse} received from the {@link com.levelup.http.HttpEngine}
 *
 * @author Created by robUx4 on 20/08/2014.
 */
public class ResponseHandler<OUTPUT> {

	public final XferTransform<HttpResponse, OUTPUT> contentParser;
	public final ErrorHandler errorHandler;

	public ResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser, ErrorHandler errorHandler) {
		if (null == contentParser) throw new NullPointerException("we need a parser for the content");
		if (null == errorHandler) throw new NullPointerException("we need an error handler, consider BaseErrorHandler");
		this.contentParser = contentParser;
		this.errorHandler = errorHandler;
	}

	public ResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser) {
		this(contentParser, BaseErrorHandler.INSTANCE);
	}

	public Boolean followsRedirect() {
		return null;
	}
}
