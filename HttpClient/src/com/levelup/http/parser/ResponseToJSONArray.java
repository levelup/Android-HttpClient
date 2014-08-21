package com.levelup.http.parser;

import org.json.JSONArray;

import com.levelup.http.HttpResponse;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class ResponseToJSONArray extends DataTransformChain<HttpResponse, JSONArray> {
	public static final ResponseToJSONArray INSTANCE = new Builder().build();

	private static class Builder extends DataTransformChain.Builder<HttpResponse, JSONArray> {
		public Builder() {
			super(DataTransformResponseInputStream.INSTANCE);
			addDataTransform(DataTransformInputStreamString.INSTANCE);
		}

		@Override
		protected DataTransformChain<HttpResponse, JSONArray> createChain(DataTransform[] transforms) {
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
