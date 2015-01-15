package co.tophe.parser;

import co.tophe.BaseResponseHandler;
import co.tophe.HttpStream;

/**
 * Helper class to transform the HTTP response body into a live {@link co.tophe.HttpStream}.
 * <p>Includes a static {@link #INSTANCE} for convenience and a default {@link #RESPONSE_HANDLER} that throws a raw/untyped
 * {@link co.tophe.ServerException ServerException}.</p>
 *
 * @author Created by robUx4 on 29/08/2014.
 */
public class BodyToHttpStream extends BodyTransformChain<HttpStream> {

	/**
	 * The instance you should use when you want to get a live {@link co.tophe.HttpStream} from an HTTP request.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setContentParser(XferTransform) BaseHttpRequest.Builder.setContentParser()
	 */
	public static final BodyToHttpStream INSTANCE = new BodyToHttpStream(
			createBuilder(XferTransformResponseInputStream.INSTANCE)
					.addDataTransform(XferTransformInputStreamHttpStream.INSTANCE)
	);

	/**
	 * An instance to use when you want to get an {@link co.tophe.HttpStream} from an HTTP request or throw a raw/untyped
	 * {@link co.tophe.ServerException ServerException} on server generated errors.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setResponseHandler(co.tophe.ResponseHandler) BaseHttpRequest.Builder.setResponseHandler()
	 */
	public static final BaseResponseHandler<HttpStream> RESPONSE_HANDLER = new BaseResponseHandler<HttpStream>(INSTANCE);

	private BodyToHttpStream(Builder<HttpStream> builder) {
		super(builder);
	}
}
