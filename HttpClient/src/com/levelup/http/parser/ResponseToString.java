package com.levelup.http.parser;

import java.io.InputStream;

import com.levelup.http.HttpResponse;
import com.levelup.http.ResponseHandler;

/**
 * Created by robUx4 on 20/08/2014.
 */
public class ResponseToString extends ResponseTransformChain<String> {
	public static final ResponseToString INSTANCE = new ResponseToString(
			ResponseTransformChain.Builder
					.init(XferTransformResponseInputStream.INSTANCE)
					.addDataTransform(XferTransformInputStreamString.INSTANCE)
	);
	public static final ResponseHandler<String> RESPONSE_HANDLER = new ResponseHandler<String>(INSTANCE);

	private ResponseToString(Builder<String> builder) {
		super(builder);
	}
}
