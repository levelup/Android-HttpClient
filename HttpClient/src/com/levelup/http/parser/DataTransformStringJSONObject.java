package com.levelup.http.parser;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.levelup.http.DataErrorException;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class DataTransformStringJSONObject implements DataTransform<String, JSONObject> {
	public static final DataTransformStringJSONObject INSTANCE = new DataTransformStringJSONObject();

	private DataTransformStringJSONObject() {
	}

	@Override
	public JSONObject transform(String srcData, ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException {
		try {
			return new JSONObject(srcData);
		} catch (JSONException e) {
			throw new ParserException("Bad JSON data", e, srcData);
		} catch (NullPointerException e) {
			throw new ParserException("Invalid JSON data", e, srcData);
		}
	}
}
