package com.levelup.http.parser;

import org.json.JSONObject;

import com.levelup.http.ResponseHandler;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class BodyToJSONObject extends BodyTransformChain<JSONObject> {
	public static final BodyToJSONObject INSTANCE = new BodyToJSONObject(
			BodyTransformChain.Builder
					.init(BodyToString.INSTANCE)
					.addDataTransform(XferTransformStringJSONObject.INSTANCE)
	);
	public static final ResponseHandler<JSONObject> RESPONSE_HANDLER = new ResponseHandler<JSONObject>(INSTANCE);

	private BodyToJSONObject(Builder<JSONObject> builder) {
		super(builder);
	}
}
