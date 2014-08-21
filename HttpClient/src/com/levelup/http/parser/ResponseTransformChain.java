package com.levelup.http.parser;

import com.levelup.http.HttpResponse;

/**
 * Created by robUx4 on 21/08/2014.
 */
public class ResponseTransformChain<T> extends DataTransformChain<HttpResponse, T> {

	public static class Builder<T> extends DataTransformChain.Builder<HttpResponse, T> {
		public Builder() {
			super(DataTransformResponseInputStream.INSTANCE);
		}

		public Builder(DataTransform<HttpResponse,?> firstTransform) {
			super(firstTransform);
		}

		@Override
		protected DataTransformChain<HttpResponse, T> createChain(DataTransform[] transforms) {
			return new ResponseTransformChain(transforms);
		}

		@Override
		public ResponseTransformChain<T> buildChain(DataTransform<?, T> lastTransform) {
			return (ResponseTransformChain<T>) super.buildChain(lastTransform);
		}
	}

	public ResponseTransformChain(Builder<T> builder, DataTransform<?, T> lastTransform) {
		super(builder, lastTransform);
	}

	protected ResponseTransformChain(DataTransform[] transforms) {
		super(transforms);
	}
}
