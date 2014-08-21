package com.levelup.http.parser;

import org.json.JSONArray;

import com.levelup.http.HttpResponse;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class ResponseToJSONArray extends ResponseTransformChain<JSONArray> {
	public static final ResponseToJSONArray INSTANCE = new Builder().build();

	private static class Builder extends ResponseTransformChain.Builder<JSONArray> {
		public Builder() {
			addDataTransform(DataTransformInputStreamString.INSTANCE);
		}

		@Override
		protected ResponseTransformChain< JSONArray> createChain(DataTransform[] transforms) {
			return new ResponseToJSONArray(transforms);
		}

		private ResponseToJSONArray build() {
			return (ResponseToJSONArray) buildChain(DataTransformStringJSONArray.INSTANCE);
		}
	}

	private ResponseToJSONArray(DataTransform[] dataTransforms) {
		super(dataTransforms);
	}
}
