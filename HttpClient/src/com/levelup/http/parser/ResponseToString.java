package com.levelup.http.parser;

import com.levelup.http.HttpResponse;

/**
 * Created by robUx4 on 20/08/2014.
 */
public class ResponseToString extends DataTransformChain<HttpResponse, String> {
	public static final ResponseToString INSTANCE = new Builder().build();
	public static final ResponseParser<String,Object> RESPONSE_PARSER = new ResponseParser<String, Object>(INSTANCE);

	private static class Builder extends DataTransformChain.Builder<HttpResponse, String> {
		public Builder() {
			super(DataTransformResponseInputStream.INSTANCE);
		}

		@Override
		protected DataTransformChain<HttpResponse, String> createChain(DataTransform[] transforms) {
			return new ResponseToString(transforms);
		}

		private ResponseToString build() {
			return (ResponseToString) buildChain(DataTransformInputStreamString.INSTANCE);
		}
	}

	private ResponseToString(DataTransform[] dataTransforms) {
		super(dataTransforms);
	}
}
