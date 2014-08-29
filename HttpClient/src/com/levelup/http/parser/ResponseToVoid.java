package com.levelup.http.parser;

import java.io.IOException;

import com.levelup.http.HttpResponse;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;
import com.levelup.http.ResponseHandler;

/**
 * Created by robUx4 on 29/08/2014.
 */
public class ResponseToVoid implements XferTransform<HttpResponse,Void> {
	public static final ResponseToVoid INSTANCE = new ResponseToVoid();
	public static final ResponseHandler<Void> RESPONSE_HANDLER = new ResponseHandler<Void>(INSTANCE);

	private ResponseToVoid() {
	}

	@Override
	public Void transformData(HttpResponse response, ImmutableHttpRequest request) throws IOException, ParserException {
		return null;
	}
}
