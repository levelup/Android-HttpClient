package com.levelup.http.parser;

import java.io.IOException;

import com.levelup.http.BaseResponseHandler;
import com.levelup.http.HttpResponse;
import com.levelup.http.ImmutableHttpRequest;

/**
 * Created by robUx4 on 29/08/2014.
 */
public class BodyToVoid implements XferTransform<HttpResponse,Void> {
	public static final BodyToVoid INSTANCE = new BodyToVoid();
	public static final BaseResponseHandler<Void> RESPONSE_HANDLER = new BaseResponseHandler<Void>(INSTANCE);

	private BodyToVoid() {
	}

	@Override
	public Void transformData(HttpResponse response, ImmutableHttpRequest request) throws IOException, ParserException {
		return null;
	}
}
