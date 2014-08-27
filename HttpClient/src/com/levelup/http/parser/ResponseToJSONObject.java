package com.levelup.http.parser;

import org.json.JSONObject;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class ResponseToJSONObject extends ResponseTransformChain<JSONObject> {
	public static final ResponseToJSONObject INSTANCE = new ResponseToJSONObject(
			ResponseTransformChain.Builder
					.init(ResponseToString.INSTANCE)
					.addDataTransform(XferTransformStringJSONObject.INSTANCE)
	);

	private ResponseToJSONObject(Builder<JSONObject> builder) {
		super(builder);
	}
}
