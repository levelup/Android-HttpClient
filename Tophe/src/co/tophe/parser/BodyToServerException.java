package co.tophe.parser;

import co.tophe.ServerException;

/**
 * Helper class to transform the HTTP response body into a raw/untyped {@link co.tophe.ServerException}.
 * <p>Includes a static {@link #INSTANCE} for convenience.</p>
 *
 * @author Created by robUx4 on 29/09/2014.
 * @see co.tophe.BaseResponseHandler
 */
public class BodyToServerException extends BodyTransformChain<ServerException> {
	/**
	 * The instance you should use when you want to get a {@link co.tophe.ServerException} from an HTTP request.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setContentParser(XferTransform) BaseHttpRequest.Builder.setContentParser()
	 */
	public static final BodyToServerException INSTANCE = new BodyToServerException(
			createBuilder(XferTransformResponseInputStream.INSTANCE)
					.addDataTransform(XferTransformInputStreamServerException.INSTANCE)
	);

	protected BodyToServerException(Builder<ServerException> builder) {
		super(builder);
	}
}
