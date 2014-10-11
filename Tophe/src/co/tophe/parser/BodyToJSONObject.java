package co.tophe.parser;

import org.json.JSONObject;

import co.tophe.BaseResponseHandler;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class BodyToJSONObject extends BodyTransformChain<JSONObject> {
	public static final BodyToJSONObject INSTANCE = new BodyToJSONObject(
			BodyTransformChain.Builder
					.init(BodyToString.INSTANCE)
					.addDataTransform(XferTransformStringJSONObject.INSTANCE)
	);
	public static final BaseResponseHandler<JSONObject> RESPONSE_HANDLER = new BaseResponseHandler<JSONObject>(INSTANCE);

	private BodyToJSONObject(Builder<JSONObject> builder) {
		super(builder);
	}
}
