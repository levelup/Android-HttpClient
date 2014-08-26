package com.levelup.http.parser;

import com.levelup.http.HttpResponseHandler;

/**
 * Created by robUx4 on 20/08/2014.
 */
public class ResponseToString extends ResponseTransformChain<String> {
	public static final ResponseToString INSTANCE = new Builder().build();
	public static final HttpResponseHandler<String> RESPONSE_HANDLER = new HttpResponseHandler<String>(INSTANCE);

	private static class Builder extends ResponseTransformChain.Builder<String> {
		public Builder() {
		}

		@Override
		protected ResponseTransformChain<String> createChain(XferTransform[] transforms) {
			return new ResponseToString(transforms);
		}

		private ResponseToString build() {
			return (ResponseToString) buildChain(XferTransformInputStreamString.INSTANCE);
		}
	}

	private ResponseToString(XferTransform[] xferTransforms) {
		super(xferTransforms);
	}
}
