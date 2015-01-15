package co.tophe.parser;

import org.json.JSONObject;

import co.tophe.BaseResponseHandler;

/**
 * Helper class to transform the HTTP response body into a {@link org.json.JSONObject}.
 * <p>Includes a static {@link #INSTANCE} and a default {@link #RESPONSE_HANDLER} that throws a raw/untyped
 * {@link co.tophe.ServerException ServerException}.</p>
 *
 * @author Created by robUx4 on 20/08/2014.
 */
public final class BodyToJSONObject extends BodyTransformChain<JSONObject> {
	/**
	 * The instance you should use when you want to get a {@link org.json.JSONObject} from an HTTP request.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setContentParser(XferTransform) BaseHttpRequest.Builder.setContentParser()
	 */
	public static final BodyToJSONObject INSTANCE = new BodyToJSONObject(
			createBuilder(BodyToString.INSTANCE)
					.addDataTransform(XferTransformStringJSONObject.INSTANCE)
	);

	/**
	 * An instance to use when you want to get a {@link org.json.JSONObject} from an HTTP request or throw a raw/untyped
	 * {@link co.tophe.ServerException ServerException} on server generated errors.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setResponseHandler(co.tophe.ResponseHandler) BaseHttpRequest.Builder.setResponseHandler()
	 */
	public static final BaseResponseHandler<JSONObject> RESPONSE_HANDLER = new BaseResponseHandler<JSONObject>(INSTANCE);

	private BodyToJSONObject(Builder<JSONObject> builder) {
		super(builder);
	}
}
