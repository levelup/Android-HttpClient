package co.tophe.parser;

import java.io.IOException;
import java.io.InputStream;

import co.tophe.HttpStream;
import co.tophe.ImmutableHttpRequest;

/**
 * Helper class to transform an {@link java.io.InputStream} into a live {@link co.tophe.HttpStream}.
 * <p>Includes a static {@link #INSTANCE} for convenience.</p>
 *
 * @author Created by robUx4 on 29/08/2014.
 */
public class XferTransformInputStreamHttpStream implements XferTransform<InputStream, HttpStream> {

	/**
	 * The instance you should use when you want to get a live {@link co.tophe.HttpStream} from an {@link java.io.InputStream}.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setContentParser(XferTransform) BaseHttpRequest.Builder.setContentParser()
	 */
	public static final XferTransformInputStreamHttpStream INSTANCE = new XferTransformInputStreamHttpStream();

	private XferTransformInputStreamHttpStream() {
	}

	@Override
	public HttpStream transformData(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException {
		return new HttpStream(inputStream, request);
	}
}
