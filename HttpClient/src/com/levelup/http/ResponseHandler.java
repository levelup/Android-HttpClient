package com.levelup.http;

import com.levelup.http.parser.XferTransform;

/**
 * Handle the body of the {@link com.levelup.http.HttpResponse} received from the {@link com.levelup.http.HttpEngine}
 *
 * @author Created by robUx4 on 20/08/2014.
 */
public class ResponseHandler<OUTPUT> {

	public final XferTransform<HttpResponse, OUTPUT> contentParser;
	public final HttpFailureHandler httpFailureHandler;

	/**
	 * {@link com.levelup.http.HttpResponse} handler, turns the HTTP body into a typed object/exception
	 * @param contentParser {@link com.levelup.http.parser.XferTransform} that will turn the body into an Object when there is no error
	 * @param httpFailureHandler {@link com.levelup.http.HttpFailureHandler} that will wrap the body in a {@link com.levelup.http.HttpFailureException} when there is a server error
	 * @see com.levelup.http.BaseHttpFailureHandler BaseHttpFailureHandler for a common httpFailureHandler
	 */
	public ResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser, HttpFailureHandler httpFailureHandler) {
		if (null == contentParser) throw new NullPointerException("we need a parser for the content");
		if (null == httpFailureHandler) throw new NullPointerException("we need an error handler, consider BaseHttpFailureHandler");
		this.contentParser = contentParser;
		this.httpFailureHandler = httpFailureHandler;
	}

	/**
	 * {@link com.levelup.http.HttpResponse} handler, turns the HTTP body into a typed object/exception
	 * <p>Use {@link com.levelup.http.BaseHttpFailureHandler} to parse the error data</p>
	 * @param contentParser {@link com.levelup.http.parser.XferTransform} that will turn the body into an Object when there is no error
	 */
	public ResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser) {
		this(contentParser, BaseHttpFailureHandler.INSTANCE);
	}

	public void onNewResponse(HttpResponse response, HttpRequest request) {
	}

	public Boolean followsRedirect() {
		return null;
	}
}
