package com.levelup.http.parser;

import com.levelup.http.HttpStream;
import com.levelup.http.ResponseHandler;

/**
 * Created by robUx4 on 29/08/2014.
 */
public class BodyToHttpStream extends BodyTransformChain<HttpStream> {
	public static final BodyToHttpStream INSTANCE = new BodyToHttpStream(
			BodyTransformChain.Builder
					.init(XferTransformResponseInputStream.INSTANCE)
					.addDataTransform(XferTransformInputStreamHttpStream.INSTANCE)
	);
	public static final ResponseHandler<HttpStream> RESPONSE_HANDLER = new ResponseHandler<HttpStream>(INSTANCE);

	private BodyToHttpStream(Builder<HttpStream> builder) {
		super(builder);
	}
}
