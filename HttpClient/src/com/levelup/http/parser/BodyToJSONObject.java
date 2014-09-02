package com.levelup.http.parser;

import org.json.JSONObject;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class BodyToJSONObject extends BodyTransformChain<JSONObject> {
	public static final BodyToJSONObject INSTANCE = new BodyToJSONObject(
			BodyTransformChain.Builder
					.init(BodyToString.INSTANCE)
					.addDataTransform(XferTransformStringJSONObject.INSTANCE)
	);

	private BodyToJSONObject(Builder<JSONObject> builder) {
		super(builder);
	}
}
