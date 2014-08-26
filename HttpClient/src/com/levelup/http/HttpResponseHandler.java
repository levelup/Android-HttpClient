package com.levelup.http;

import java.io.IOException;

import com.levelup.http.DataErrorException;
import com.levelup.http.HttpResponse;
import com.levelup.http.HttpResponseErrorHandler;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;
import com.levelup.http.parser.XferTransform;

/**
 * Created by robUx4 on 20/08/2014.
 */
public class HttpResponseHandler<OUTPUT> {

	public final XferTransform<HttpResponse, OUTPUT> contentParser;
	public final HttpResponseErrorHandler errorHandler;

	public HttpResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser, HttpResponseErrorHandler errorHandler) {
		if (null == contentParser) throw new NullPointerException("we need a parser for the content");
		this.contentParser = contentParser;
		this.errorHandler = errorHandler;
	}

	public HttpResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser) {
		this(contentParser, BaseHttpResponseErrorHandler.INSTANCE);
	}
}
