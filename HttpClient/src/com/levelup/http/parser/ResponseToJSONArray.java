package com.levelup.http.parser;

import org.json.JSONArray;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class ResponseToJSONArray extends ResponseTransformChain<JSONArray> {
	public static final ResponseToJSONArray INSTANCE = new ResponseToJSONArray(
			ResponseTransformChain.Builder
					.init(ResponseToString.INSTANCE)
					.addDataTransform(XferTransformStringJSONArray.INSTANCE)
	);

	private ResponseToJSONArray(Builder<JSONArray> builder) {
		super(builder);
	}
}
