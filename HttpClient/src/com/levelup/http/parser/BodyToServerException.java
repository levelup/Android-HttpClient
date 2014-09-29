package com.levelup.http.parser;

import com.levelup.http.BaseResponseHandler;
import com.levelup.http.ServerException;

/**
 * Created by robUx4 on 29/09/2014.
 */
public class BodyToServerException extends BodyTransformChain<ServerException> {
	public static final BodyToServerException INSTANCE = new BodyToServerException(
			BodyTransformChain.Builder
					.init(XferTransformResponseInputStream.INSTANCE)
					.addDataTransform(XferTransformInputStreamServerException.INSTANCE)
	);
	public static final BaseResponseHandler<ServerException> RESPONSE_HANDLER = new BaseResponseHandler<ServerException>(INSTANCE);

	protected BodyToServerException(Builder<ServerException> builder) {
		super(builder);
	}
}
