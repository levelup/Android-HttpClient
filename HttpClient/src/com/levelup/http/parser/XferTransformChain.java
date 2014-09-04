package com.levelup.http.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.levelup.http.ImmutableHttpRequest;

/**
 * A {@link com.levelup.http.parser.XferTransform} that uses multiple sub {@link com.levelup.http.parser.XferTransform} to generate the output
 *
 * @author Created by robUx4 on 20/08/2014.
 */
public class XferTransformChain<INPUT, OUTPUT> implements XferTransform<INPUT, OUTPUT> {

	public final XferTransform[] transforms;

	public static class Builder<INPUT, OUTPUT> {
		private ArrayList<XferTransform> transforms = new ArrayList<XferTransform>();

		protected Builder() {
		}

		public <V> Builder<INPUT, V> addDataTransform(XferTransform<OUTPUT, V> intermediateTransform) {
			if (intermediateTransform instanceof XferTransformChain) {
				// flatten the list of transforms
				XferTransformChain transform = (XferTransformChain) intermediateTransform;
				transforms.addAll(Arrays.asList(transform.transforms));
			} else {
				transforms.add(intermediateTransform);
			}
			return (Builder<INPUT, V>) this;
		}

		public XferTransformChain<INPUT, OUTPUT> build() {
			return buildInstance(this);
		}

		public static <K, L, B extends Builder<K, L>> B start(XferTransform<K, L> pipe, B builder) {
			((Builder) builder).transforms = new ArrayList<XferTransform>();
			if (pipe instanceof XferTransformChain) {
				XferTransformChain xferTransformChain = (XferTransformChain) pipe;
				((Builder) builder).transforms.addAll(Arrays.asList(xferTransformChain.transforms));
			} else {
				((Builder) builder).transforms.add(pipe);
			}
			return builder;
		}

		public static <K, L> Builder<K, L> start(XferTransform<K, L> pipe) {
			return start(pipe, new Builder<K, L>());
		}

		protected XferTransformChain<INPUT, OUTPUT> buildInstance(Builder<INPUT, OUTPUT> builder) {
			return new XferTransformChain<INPUT, OUTPUT>(builder);
		}
	}

	protected XferTransformChain(Builder<INPUT, OUTPUT> builder) {
		this.transforms = builder.transforms.toArray(new XferTransform[builder.transforms.size()]);
	}

	private XferTransformChain(XferTransform[] transforms) {
		if (null==transforms) throw new NullPointerException();
		this.transforms = transforms;
	}

	@Override
	public OUTPUT transformData(INPUT input, ImmutableHttpRequest request) throws IOException, ParserException {
		Object intermediate = input;
		for (XferTransform transform : transforms) {
			try {
				intermediate = transform.transformData(intermediate, request);
			} catch (ClassCastException e) {
				throw new ParserException("Can't cast "+intermediate+" using "+transform+" in "+this, e, null);
			}
		}
		return (OUTPUT) intermediate;
	}

	public XferTransformChain<?,OUTPUT> skipFirstTransform() {
		return new XferTransformChain<Object, OUTPUT>(Arrays.copyOfRange(transforms, 1, transforms.length));
	}

	public XferTransformChain<INPUT,?> removeLastTransform() {
		if (transforms.length==0)
			return null;
		return new XferTransformChain<INPUT, Object>(Arrays.copyOfRange(transforms, 0, transforms.length-1));
	}

	@Override
	public String toString() {
		return '{' + super.toString() + " transforms:" + Arrays.toString(transforms) + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (o==this) return true;
		if (!(o instanceof XferTransformChain)) return false;
		XferTransformChain oc = (XferTransformChain) o;
		return Arrays.equals(oc.transforms, transforms);
	}
}
