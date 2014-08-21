package com.levelup.http.parser;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import com.levelup.http.DataErrorException;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class DataTransformStringJSONArray implements DataTransform<String, JSONArray> {
	public static final DataTransformStringJSONArray INSTANCE = new DataTransformStringJSONArray();

	private DataTransformStringJSONArray() {
	}

	@Override
	public JSONArray transform(String srcData, ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException {
		try {
			return new JSONArray(srcData);
		} catch (JSONException e) {
			throw new ParserException("Bad JSON data", e, srcData);
		} catch (NullPointerException e) {
			throw new ParserException("Invalid JSON data", e, srcData);
		}
	}
}
