package co.tophe.parser;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.test.AndroidTestCase;

import co.tophe.ImmutableHttpRequest;

public class XferTransformChainTest extends AndroidTestCase {

	public static class XferTransformChain<S, T> {
		private XferTransform[] transforms;

		public static class Builder<INPUT, OUTPUT> {
			private List<XferTransform> transforms = new ArrayList<XferTransform>();

			private Builder() {
			}

			public static <INPUT> Builder<INPUT,INPUT> newBuilder() {
				return new Builder();
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
				return new XferTransformChain<INPUT,OUTPUT>(transforms.toArray(new XferTransform[transforms.size()]));
			}

			public static <K, L> Builder<K, L> start(XferTransform<K, L> pipe) {
				Builder<K, L> builder = new Builder<K, L>();
				builder.transforms.add(pipe);
				return builder;
			}
		}

		private XferTransformChain(XferTransform[] xferTransformChains) {
			transforms = xferTransformChains;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public T run(S s) {
			Object source = s;
			Object target = null;
			for (XferTransform p : transforms) {
				try {target = p.transformData(source, null);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserException e) {
					e.printStackTrace();
				}
				source = target;
			}
			return (T) target;
		}
	}

	public void testPipe() {
		XferTransform<String, Integer> pipe1 = new XferTransform<String, Integer>() {
			@Override
			public Integer transformData(String s, ImmutableHttpRequest req) throws IOException, ParserException {
				return Integer.valueOf(s);
			}
		};
		XferTransform<Integer, Long> pipe2 = new XferTransform<Integer, Long>() {
			@Override
			public Long transformData(Integer s, ImmutableHttpRequest req) throws IOException, ParserException {
				return s.longValue();
			}
		};
		XferTransform<Long, BigInteger> pipe3 = new XferTransform<Long, BigInteger>() {
			@Override
			public BigInteger transformData(Long s, ImmutableHttpRequest req) throws IOException, ParserException {
				return new BigInteger(s.toString());
			}
		};
		//XferTransformChain<String, BigInteger> chain = XferTransformChain.start(pipe1).append(pipe2).append(pipe3);
		//BigInteger result = chain.run("12");

		XferTransformChain.Builder<String,String> builder = XferTransformChain.Builder.newBuilder();
		XferTransformChain<String, String> build0 = builder.build();
		XferTransformChain.Builder<String,Integer> b1 = builder.addDataTransform(pipe1);
		XferTransformChain<String, Integer> build1 = b1.build();
		XferTransformChain.Builder<String, Long> b2 = b1.addDataTransform(pipe2);
		XferTransformChain.Builder<String, BigInteger> b3 = b2.addDataTransform(pipe3);
		XferTransformChain<String, BigInteger> chain1 = b3.build();
		BigInteger result1 = chain1.run("12");

		XferTransformChain<String, BigInteger> chain2 = XferTransformChain.Builder.start(pipe1).addDataTransform(pipe2).addDataTransform(pipe3).build();
		BigInteger result2 = chain2.run("12");
	}
/*
	public void testChaining1() throws Exception {
		XferTransformChain.Builder<HttpResponse, JSONObject> builder = new XferTransformChain.Builder<HttpResponse, JSONObject>();

		XferTransformChain.Builder<HttpResponse, JSONObject> a = builder.addChain1(XferTransformResponseInputStream.INSTANCE);
		XferTransformChain.Builder<InputStream, JSONObject> b = a.addChain1(XferTransformInputStreamString.INSTANCE);
		XferTransformChain.Builder<String, JSONObject> c = b.addChain1(XferTransformStringJSONObject.INSTANCE);

		XferTransformChain<HttpResponse, JSONObject> chain = c.build();
	}

	public void testChaining2() throws Exception {
		XferTransformChain.Builder<HttpResponse, JSONObject> builder = new XferTransformChain.Builder<HttpResponse, JSONObject>();

		XferTransformChain.Builder<HttpResponse, InputStream> a = builder.addChain2(XferTransformResponseInputStream.INSTANCE);
		XferTransformChain.Builder<HttpResponse, String> b = a.addChain2(XferTransformInputStreamString.INSTANCE);
		XferTransformChain.Builder<HttpResponse, JSONObject> c = b.addChain2(XferTransformStringJSONObject.INSTANCE);

		XferTransformChain<HttpResponse, JSONObject> chain = c.build();
	}

	public void testChaining2NOK() throws Exception {
		XferTransformChain.Builder<HttpResponse, JSONObject> builder = new XferTransformChain.Builder<HttpResponse, JSONObject>();

		XferTransformChain.Builder<HttpResponse, InputStream> a = builder.addChain2(XferTransformResponseInputStream.INSTANCE);
		a.addChain2(XferTransformResponseInputStream.INSTANCE);
		a.addChain2(XferTransformInputStreamString.INSTANCE);
		XferTransformChain.Builder<HttpResponse, String> b = a.addChain2(XferTransformInputStreamString.INSTANCE);
		XferTransformChain.Builder<HttpResponse, JSONObject> c = b.addChain2(XferTransformStringJSONObject.INSTANCE);

		XferTransformChain<HttpResponse, JSONObject> chain = c.build();
	}
*/
}