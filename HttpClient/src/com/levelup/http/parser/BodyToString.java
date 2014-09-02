package com.levelup.http.parser;

import com.levelup.http.ResponseHandler;

/**
 * Created by robUx4 on 20/08/2014.
 */
public class BodyToString extends BodyTransformChain<String> {
	public static final BodyToString INSTANCE = new BodyToString(
			BodyTransformChain.Builder
					.init(XferTransformResponseInputStream.INSTANCE)
					.addDataTransform(XferTransformInputStreamString.INSTANCE)
	);
	public static final ResponseHandler<String> RESPONSE_HANDLER = new ResponseHandler<String>(INSTANCE);

	private BodyToString(Builder<String> builder) {
		super(builder);
	}
}
