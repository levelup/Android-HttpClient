package co.tophe.parser;

import org.json.JSONArray;

import co.tophe.BaseResponseHandler;

/**
 * Helper class to transform the HTTP response body into a {@link org.json.JSONArray}.
 * <p>Includes a static {@link #INSTANCE} for convenience and a default {@link #RESPONSE_HANDLER} that throws a raw/untyped
 * {@link co.tophe.ServerException ServerException}.</p>
 *
 * @author Created by robUx4 on 20/08/2014.
 */
public final class BodyToJSONArray extends BodyTransformChain<JSONArray> {
	/**
	 * The instance you should use when you want to get a {@link org.json.JSONArray} from an HTTP request.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setContentParser(XferTransform) BaseHttpRequest.Builder.setContentParser()
	 */
	public static final BodyToJSONArray INSTANCE = new BodyToJSONArray(
			createBuilder(BodyToString.INSTANCE)
					.addDataTransform(XferTransformStringJSONArray.INSTANCE)
	);

	/**
	 * An instance to use when you want to get a {@link org.json.JSONArray} from an HTTP request or throw a raw/untyped
	 * {@link co.tophe.ServerException ServerException} on server generated errors.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setResponseHandler(co.tophe.ResponseHandler) BaseHttpRequest.Builder.setResponseHandler()
	 */
	public static final BaseResponseHandler<JSONArray> RESPONSE_HANDLER = new BaseResponseHandler<JSONArray>(INSTANCE);

	private BodyToJSONArray(Builder<JSONArray> builder) {
		super(builder);
	}
}
