package co.tophe.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.support.annotation.NonNull;

import co.tophe.ImmutableHttpRequest;

/**
 * A {@link XferTransform} that uses multiple sub {@link XferTransform} to generate the output
 *
 * @author Created by robUx4 on 20/08/2014.
 * @see co.tophe.parser.BodyTransformChain
 * @see co.tophe.parser.XferTransformChain.Builder
 * @see #initBuilder(XferTransform, co.tophe.parser.XferTransformChain.Builder)
 */
public class XferTransformChain<INPUT, OUTPUT> implements XferTransform<INPUT, OUTPUT> {

	public final XferTransform[] transforms;

	/**
	 * Initialize a new {@link co.tophe.parser.XferTransformChain.Builder} with its first transformation.
	 *
	 * @param pipe     the first transformation of the Builder.
	 * @param builder  the Builder to initialize.
	 * @param <INPUT>  the input type for the Builder
	 * @param <OUTPUT> the output type for the Builder
	 * @param <B>      the type of the Builder being initialized.
	 * @return initialize the Builder.
	 * @see co.tophe.parser.BodyTransformChain.Builder#init(co.tophe.parser.XferTransform)
	 */
	public static <INPUT, OUTPUT, B extends Builder<INPUT, OUTPUT>> B initBuilder(XferTransform<INPUT, OUTPUT> pipe, B builder) {
		((Builder) builder).transforms = new ArrayList<XferTransform>();
		if (pipe instanceof XferTransformChain) {
			XferTransformChain xferTransformChain = (XferTransformChain) pipe;
			((Builder) builder).transforms.addAll(Arrays.asList(xferTransformChain.transforms));
		} else {
			((Builder) builder).transforms.add(pipe);
		}
		return builder;
	}

	/**
	 * Builder to chain multiple transformations from {@link INPUT} to {@link OUTPUT} type.
	 * <p>Start your Builder chain with {@link #initBuilder(XferTransform, co.tophe.parser.XferTransformChain.Builder)},
	 * add transformations with {@link #addDataTransform(XferTransform)} and finish by calling {@link #build()}on the last Builder in the chain.</p>
	 *
	 * @param <INPUT>  the input type for the built {@link co.tophe.parser.XferTransformChain}
	 * @param <OUTPUT> the output type for the built {@link co.tophe.parser.XferTransformChain}
	 * @see co.tophe.parser.XferTransformChain.Builder
	 */
	public static class Builder<INPUT, OUTPUT> {
		private ArrayList<XferTransform> transforms = new ArrayList<XferTransform>();

		protected Builder() {
		}

		/**
		 * Add a transformation to the chain. The returned builder now transforms from the called builder {@link INPUT} type to the
		 * {@link NEW_OUTPUT} type of the added transformation.
		 *
		 * @param intermediateTransform the transformation to add at the end of the chain.
		 * @param <NEW_OUTPUT>          the OUTPUT type of the returned builder.
		 */
		public <NEW_OUTPUT> Builder<INPUT, NEW_OUTPUT> addDataTransform(XferTransform<OUTPUT, NEW_OUTPUT> intermediateTransform) {
			if (intermediateTransform instanceof XferTransformChain) {
				// flatten the list of transforms
				XferTransformChain transform = (XferTransformChain) intermediateTransform;
				transforms.addAll(Arrays.asList(transform.transforms));
			} else {
				transforms.add(intermediateTransform);
			}
			return (Builder<INPUT, NEW_OUTPUT>) this;
		}

		/**
		 * Build the chained transformation.
		 * <p>If you are overriding a {@link co.tophe.parser.XferTransformChain} class, you need to implement
		 * {@link #buildInstance(co.tophe.parser.XferTransformChain.Builder)} in it's Builder.</p>
		 */
		public final XferTransformChain<INPUT, OUTPUT> build() {
			return buildInstance(this);
		}

		/**
		 * Create the {@link co.tophe.parser.XferTransformChain} from the Builder.
		 */
		protected XferTransformChain<INPUT, OUTPUT> buildInstance(Builder<INPUT, OUTPUT> builder) {
			return new XferTransformChain<INPUT, OUTPUT>(builder);
		}
	}

	/**
	 * Constructor using the {@link co.tophe.parser.XferTransformChain.Builder}.
	 */
	protected XferTransformChain(Builder<INPUT, OUTPUT> builder) {
		this.transforms = builder.transforms.toArray(new XferTransform[builder.transforms.size()]);
	}

	private XferTransformChain(@NonNull XferTransform[] transforms) {
		if (null == transforms) throw new NullPointerException();
		this.transforms = transforms;
	}

	@Override
	public OUTPUT transformData(INPUT input, ImmutableHttpRequest request) throws IOException, ParserException {
		Object intermediate = input;
		for (XferTransform transform : transforms) {
			try {
				intermediate = transform.transformData(intermediate, request);
			} catch (ClassCastException e) {
				throw new ParserException("Can't cast " + intermediate + " using " + transform + " in " + this, e, null);
			}
		}
		return (OUTPUT) intermediate;
	}

	/**
	 * Get a {@link co.tophe.parser.XferTransformChain} with its first transformation removed.
	 */
	public XferTransformChain<?, OUTPUT> skipFirstTransform() {
		return new XferTransformChain<Object, OUTPUT>(Arrays.copyOfRange(transforms, 1, transforms.length));
	}

	/**
	 * Get a {@link co.tophe.parser.XferTransformChain} with its last transformation removed.
	 */
	public XferTransformChain<INPUT, ?> removeLastTransform() {
		if (transforms.length == 0)
			return null;
		return new XferTransformChain<INPUT, Object>(Arrays.copyOfRange(transforms, 0, transforms.length - 1));
	}

	@Override
	public String toString() {
		return '{' + super.toString() + " transforms:" + Arrays.toString(transforms) + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof XferTransformChain)) return false;
		XferTransformChain oc = (XferTransformChain) o;
		return Arrays.equals(oc.transforms, transforms);
	}
}
