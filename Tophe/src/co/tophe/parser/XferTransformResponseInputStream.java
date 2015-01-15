package co.tophe.parser;

import java.io.IOException;
import java.io.InputStream;

import co.tophe.HttpResponse;
import co.tophe.ImmutableHttpRequest;

/**
 * Helper class to transform an {@link co.tophe.HttpResponse} into an {@link java.io.InputStream}.
 * <p>Includes a static {@link #INSTANCE} for convenience.</p>
 *
 * @author Created by robUx4 on 20/08/2014.
 */
public final class XferTransformResponseInputStream implements XferTransform<HttpResponse, InputStream> {
	// This is a special class as it depends on the engine used, unlike other XferTransform

	/**
	 * The instance you should use when you want to get an {@link java.io.InputStream} from an {@link co.tophe.HttpResponse}.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setContentParser(XferTransform) BaseHttpRequest.Builder.setContentParser()
	 */
	public static final XferTransformResponseInputStream INSTANCE = new XferTransformResponseInputStream();

	private XferTransformResponseInputStream() {
	}

	@Override
	public InputStream transformData(HttpResponse response, ImmutableHttpRequest request) throws IOException, ParserException {
		return response.getContentStream();
	}
}
