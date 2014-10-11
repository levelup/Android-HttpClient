package co.tophe.parser;

import org.json.JSONArray;

import co.tophe.BaseResponseHandler;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class BodyToJSONArray extends BodyTransformChain<JSONArray> {
	public static final BodyToJSONArray INSTANCE = new BodyToJSONArray(
			BodyTransformChain.Builder
					.init(BodyToString.INSTANCE)
					.addDataTransform(XferTransformStringJSONArray.INSTANCE)
	);
	public static final BaseResponseHandler<JSONArray> RESPONSE_HANDLER = new BaseResponseHandler<JSONArray>(INSTANCE);

	private BodyToJSONArray(Builder<JSONArray> builder) {
		super(builder);
	}
}
