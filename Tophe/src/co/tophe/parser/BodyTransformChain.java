package co.tophe.parser;

import java.io.InputStream;

import co.tophe.HttpResponse;

/**
 * Created by robUx4 on 21/08/2014.
 */
public class BodyTransformChain<T> extends XferTransformChain<HttpResponse, T> {

	public static class Builder<T> extends XferTransformChain.Builder<HttpResponse, T> {

		public static <T> Builder<T> init(XferTransform<HttpResponse, T> firstTransform) {
			return start(firstTransform, new Builder<T>());
		}

		@Override
		protected XferTransformChain<HttpResponse, T> buildInstance(XferTransformChain.Builder<HttpResponse, T> builder) {
			return new BodyTransformChain((Builder) builder);
		}

		@Override
		public <V> Builder<V> addDataTransform(XferTransform<T, V> endTransform) {
			return (Builder<V>) super.addDataTransform(endTransform);
		}
	}

	/**
	 * Helper constructor to do a single transformation from InputStream
	 * @param endTransform
	 */
	public BodyTransformChain(XferTransform<InputStream, T> endTransform) {
		this(Builder.init(XferTransformResponseInputStream.INSTANCE).addDataTransform(endTransform));
	}

	protected BodyTransformChain(Builder<T> builder) {
		super(builder);
	}
}
