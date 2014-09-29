package com.levelup.http;

import com.levelup.http.parser.XferTransform;

/**
 * Created by robUx4 on 29/09/2014.
 */
public class BaseResponseHandler<OUTPUT> extends ResponseHandler<OUTPUT, ServerException> {

	/**
	 * {@link com.levelup.http.HttpResponse} handler, turns the HTTP body into a typed object/exception
	 * <p>Use {@link BaseServerErrorHandler} to parse the error data</p>
	 *
	 * @param contentParser {@link com.levelup.http.parser.XferTransform} that will turn the body into an Object when there is no error
	 */
	public BaseResponseHandler(XferTransform<HttpResponse, OUTPUT> contentParser) {
		super(contentParser, BaseServerErrorHandler.INSTANCE);
	}
}
