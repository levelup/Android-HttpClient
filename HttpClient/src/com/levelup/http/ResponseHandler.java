package com.levelup.http;

import com.levelup.http.parser.XferTransform;

/**
 * Handle the body of the {@link com.levelup.http.HttpResponse} received from the {@link com.levelup.http.HttpEngine}
 * @param <OUTPUT> Type of the Object that the HTTP call will return
 * @param <SE> Type of the Exception that will be raised when the server reports an error
 * @author Created by robUx4 on 20/08/2014.
 */
public class ResponseHandler<OUTPUT, SE extends ServerException> {

	public final XferTransform<HttpResponse, OUTPUT> contentParser;
	public final XferTransform<HttpResponse, SE> errorParser;

	public ResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser, XferTransform<HttpResponse, SE> errorParser) {
		this.contentParser = contentParser;
		this.errorParser = errorParser;
	}

	public void onNewResponse(HttpResponse response, HttpRequest request) {
	}

	public Boolean followsRedirect() {
		return null;
	}
}
