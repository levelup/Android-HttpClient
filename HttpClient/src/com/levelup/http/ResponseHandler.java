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
	 * Main Constructor
	 * @param contentParser
	 * @param httpFailureHandler
	 * @see com.levelup.http.parser.HttpFailureHandlerViaXferTransform HttpFailureHandlerViaXferTransform for a common httpFailureHandler
	 */
	public ResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser, HttpFailureHandler httpFailureHandler) {
		if (null == contentParser) throw new NullPointerException("we need a parser for the content");
		if (null == httpFailureHandler) throw new NullPointerException("we need an error handler, consider BaseHttpFailureHandler");
		this.contentParser = contentParser;
		this.httpFailureHandler = httpFailureHandler;
	}

	public ResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser) {
		this(contentParser, BaseHttpFailureHandler.INSTANCE);
	}

	public void onNewResponse(HttpResponse response, HttpRequest request) {
	}

	public Boolean followsRedirect() {
		return null;
	}
}
