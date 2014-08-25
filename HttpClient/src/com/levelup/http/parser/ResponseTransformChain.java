package com.levelup.http.parser;

import com.levelup.http.HttpResponse;

/**
 * Created by robUx4 on 21/08/2014.
 */
public class ResponseTransformChain<T> extends XferTransformChain<HttpResponse, T> {

	public static class Builder<T> extends XferTransformChain.Builder<HttpResponse, T> {
		public Builder() {
			super(XferTransformResponseInputStream.INSTANCE);
		}

		public Builder(XferTransform<HttpResponse,?> firstTransform) {
			super(firstTransform);
		}

		@Override
		protected XferTransformChain<HttpResponse, T> createChain(XferTransform[] transforms) {
			return new ResponseTransformChain(transforms);
		}

		@Override
		public ResponseTransformChain<T> buildChain(XferTransform<?, T> lastTransform) {
			return (ResponseTransformChain<T>) super.buildChain(lastTransform);
		}
	}

	public ResponseTransformChain(Builder<T> builder, XferTransform<?, T> lastTransform) {
		super(builder, lastTransform);
	}

	protected ResponseTransformChain(XferTransform[] transforms) {
		super(transforms);
	}
}
