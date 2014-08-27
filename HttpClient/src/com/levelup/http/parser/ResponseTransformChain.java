package com.levelup.http.parser;

import java.io.InputStream;

import com.levelup.http.HttpResponse;

/**
 * Created by robUx4 on 21/08/2014.
 */
public class ResponseTransformChain<T> extends XferTransformChain<HttpResponse, T> {

	public static class Builder<T> extends XferTransformChain.Builder<HttpResponse, T> {

		public static <T> Builder<T> init(XferTransform<HttpResponse, T> firstTransform) {
			return XferTransformChain.Builder.start(firstTransform, new Builder<T>());
		}

		@Override
		protected XferTransformChain<HttpResponse, T> buildInstance(XferTransformChain.Builder<HttpResponse, T> builder) {
			return new ResponseTransformChain((Builder) builder);
		}

		@Override
		public <V> Builder<V> addDataTransform(XferTransform<T, V> endTransform) {
			return (Builder<V>) super.addDataTransform(endTransform);
		}
	}

	public ResponseTransformChain(XferTransform<InputStream, T> endTransform) {
		this(Builder.init(XferTransformResponseInputStream.INSTANCE).addDataTransform(endTransform));
	}

	protected ResponseTransformChain(Builder<T> builder) {
		super(builder);
	}
}
