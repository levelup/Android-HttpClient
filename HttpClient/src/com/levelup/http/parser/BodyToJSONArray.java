package com.levelup.http.parser;

import org.json.JSONArray;

import com.levelup.http.ResponseHandler;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class BodyToJSONArray extends BodyTransformChain<JSONArray> {
	public static final BodyToJSONArray INSTANCE = new BodyToJSONArray(
			BodyTransformChain.Builder
					.init(BodyToString.INSTANCE)
					.addDataTransform(XferTransformStringJSONArray.INSTANCE)
	);
	public static final ResponseHandler<JSONArray> RESPONSE_HANDLER = new ResponseHandler<JSONArray>(INSTANCE);

	private BodyToJSONArray(Builder<JSONArray> builder) {
		super(builder);
	}
}
