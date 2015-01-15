package co.tophe.parser;

import co.tophe.BaseResponseHandler;

/**
 * Helper class to transform the HTTP response body into a {@link java.lang.String}.
 * <p>Includes a static {@link #INSTANCE} and a default {@link #RESPONSE_HANDLER} that throws a raw/untyped
 * {@link co.tophe.ServerException ServerException}.</p>
 * <p>This is discouraged as TOPHE offers a strong typing of the received data.</p>
 *
 * @author Created by robUx4 on 20/08/2014.
 */
public class BodyToString extends BodyTransformChain<String> {
	/**
	 * The instance you should use when you want to get a {@link java.lang.String} from an HTTP request.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setContentParser(XferTransform) BaseHttpRequest.Builder.setContentParser()
	 */
	public static final BodyToString INSTANCE = new BodyToString(
			BodyTransformChain.Builder
					.init(XferTransformResponseInputStream.INSTANCE)
					.addDataTransform(XferTransformInputStreamString.INSTANCE)
	);

	/**
	 * An instance to use when you want to get a {@link java.lang.String} from an HTTP request or throw a raw/untyped
	 * {@link co.tophe.ServerException ServerException} on server generated errors.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setResponseHandler(co.tophe.ResponseHandler) BaseHttpRequest.Builder.setResponseHandler()
	 */
	public static final BaseResponseHandler<String> RESPONSE_HANDLER = new BaseResponseHandler<String>(INSTANCE);

	private BodyToString(Builder<String> builder) {
		super(builder);
	}
}
