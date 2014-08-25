package com.levelup.http.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * A {@link com.levelup.http.parser.XferTransform} that uses multiple sub {@link com.levelup.http.parser.XferTransform} to generate the output
 *
 * @author Created by robUx4 on 20/08/2014.
 */
public class XferTransformChain<INPUT, OUTPUT> implements XferTransform<INPUT, OUTPUT> {

	public final XferTransform[] transforms;

	public static class Builder<INPUT, OUTPUT> {
		private ArrayList<XferTransform> transforms = new ArrayList<XferTransform>();

		public Builder(XferTransform<INPUT,?> firstTransform) {
			addDataTransform(firstTransform);
		}

		public final Builder<INPUT,OUTPUT> addDataTransform(XferTransform<?, ?> intermediateTransform) {
			if (intermediateTransform instanceof XferTransformChain) {
				// flatten the list of transforms
				XferTransformChain transform = (XferTransformChain) intermediateTransform;
				transforms.addAll(Arrays.asList(transform.transforms));
			} else {
				transforms.add(intermediateTransform);
			}
			return this;
		}

		public XferTransformChain<INPUT, OUTPUT> buildChain(XferTransform<?,OUTPUT> lastTransform) {
			addDataTransform(lastTransform);
			return createChain(transforms.toArray(new XferTransform[transforms.size()]));
		}

		protected XferTransformChain<INPUT, OUTPUT> createChain(XferTransform[] transforms) {
			return new XferTransformChain<INPUT, OUTPUT>(transforms);
		}
	}

	public XferTransformChain(Builder<INPUT, OUTPUT> builder, XferTransform<?, OUTPUT> lastTransform) {
		builder.addDataTransform(lastTransform);
		this.transforms = builder.transforms.toArray(new XferTransform[builder.transforms.size()]);
	}

	protected XferTransformChain(XferTransform[] transforms) {
		if (null==transforms) throw new NullPointerException();
		this.transforms = transforms;
	}

	@Override
	public OUTPUT transform(INPUT input, ImmutableHttpRequest request) throws IOException, ParserException {
		Object intermediate = input;
		for (XferTransform transform : transforms) {
			try {
				intermediate = transform.transform(intermediate, request);
			} catch (ClassCastException e) {
				throw new ParserException("Can't cast "+intermediate+" using "+transform+" in "+this, e, null);
			}
		}
		return (OUTPUT) intermediate;
	}

	public XferTransformChain<?,OUTPUT> skipFirstTransform() {
		return new XferTransformChain<Object, OUTPUT>(Arrays.copyOfRange(transforms, 1, transforms.length));
	}

}
