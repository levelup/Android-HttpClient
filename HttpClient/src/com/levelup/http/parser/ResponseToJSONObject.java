package com.levelup.http.parser;

import org.json.JSONObject;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class ResponseToJSONObject extends ResponseTransformChain<JSONObject> {
	public static final ResponseToJSONObject INSTANCE = new Builder().build();

	private static class Builder extends ResponseTransformChain.Builder<JSONObject> {
		public Builder() {
			addDataTransform(XferTransformInputStreamString.INSTANCE);
		}

		@Override
		protected ResponseTransformChain<JSONObject> createChain(XferTransform[] transforms) {
			return new ResponseToJSONObject(transforms);
		}

		private ResponseToJSONObject build() {
			return (ResponseToJSONObject) buildChain(XferTransformStringJSONObject.INSTANCE);
		}
	}

	private ResponseToJSONObject(XferTransform[] xferTransforms) {
		super(xferTransforms);
	}
}
