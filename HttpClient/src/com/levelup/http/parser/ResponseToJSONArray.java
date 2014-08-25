package com.levelup.http.parser;

import org.json.JSONArray;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class ResponseToJSONArray extends ResponseTransformChain<JSONArray> {
	public static final ResponseToJSONArray INSTANCE = new Builder().build();

	private static class Builder extends ResponseTransformChain.Builder<JSONArray> {
		public Builder() {
			addDataTransform(XferTransformInputStreamString.INSTANCE);
		}

		@Override
		protected ResponseTransformChain< JSONArray> createChain(XferTransform[] transforms) {
			return new ResponseToJSONArray(transforms);
		}

		private ResponseToJSONArray build() {
			return (ResponseToJSONArray) buildChain(XferTransformStringJSONArray.INSTANCE);
		}
	}

	private ResponseToJSONArray(XferTransform[] xferTransforms) {
		super(xferTransforms);
	}
}
