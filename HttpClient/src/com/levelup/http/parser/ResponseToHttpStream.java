package com.levelup.http.parser;

import com.levelup.http.HttpStream;
import com.levelup.http.ResponseHandler;

/**
 * Created by robUx4 on 29/08/2014.
 */
public class ResponseToHttpStream extends ResponseTransformChain<HttpStream> {
	public static final ResponseToHttpStream INSTANCE = new ResponseToHttpStream(
			ResponseTransformChain.Builder
					.init(XferTransformResponseInputStream.INSTANCE)
					.addDataTransform(XferTransformInputStreamHttpStream.INSTANCE)
	);
	public static final ResponseHandler<HttpStream> RESPONSE_HANDLER = new ResponseHandler<HttpStream>(INSTANCE);

	private ResponseToHttpStream(Builder<HttpStream> builder) {
		super(builder);
	}
}
