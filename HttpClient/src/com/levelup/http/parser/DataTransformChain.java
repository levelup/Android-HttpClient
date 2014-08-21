package com.levelup.http.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.levelup.http.DataErrorException;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * Created by robUx4 on 20/08/2014.
 */
public class DataTransformChain<INPUT, OUTPUT> implements DataTransform<INPUT, OUTPUT> {

	public final DataTransform[] transforms;

	public static class Builder<INPUT, OUTPUT> {
		private ArrayList<DataTransform> transforms = new ArrayList<DataTransform>();

		public Builder(DataTransform<INPUT,?> firstTransform) {
			addDataTransform(firstTransform);
		}

		public final Builder<INPUT,OUTPUT> addDataTransform(DataTransform<?, ?> intermediateTransform) {
			if (intermediateTransform instanceof DataTransformChain) {
				// flatten the list of transforms
				DataTransformChain transform = (DataTransformChain) intermediateTransform;
				transforms.addAll(Arrays.asList(transform.transforms));
			} else {
				transforms.add(intermediateTransform);
			}
			return this;
		}

		public final DataTransformChain<INPUT, OUTPUT> buildChain(DataTransform<?,OUTPUT> lastTransform) {
			addDataTransform(lastTransform);
			return createChain(transforms.toArray(new DataTransform[transforms.size()]));
		}

		protected DataTransformChain<INPUT, OUTPUT> createChain(DataTransform[] transforms) {
			return new DataTransformChain<INPUT, OUTPUT>(transforms);
		}
	}

	protected DataTransformChain(Builder<INPUT, OUTPUT> builder, DataTransform<?, OUTPUT> lastTransform) {
		builder.addDataTransform(lastTransform);
		this.transforms = builder.transforms.toArray(new DataTransform[builder.transforms.size()]);
	}

	protected DataTransformChain(DataTransform[] transforms) {
		this.transforms = transforms;
	}

	@Override
	public OUTPUT transform(INPUT input, ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException {
		Object intermediate = input;
		for (DataTransform transform : transforms) {
			try {
				intermediate = transform.transform(intermediate, request);
			} catch (ClassCastException e) {
				throw new ParserException("Can't cast "+intermediate+" using "+transform+" in "+this, e, null);
			}
		}
		return (OUTPUT) intermediate;
	}
}
