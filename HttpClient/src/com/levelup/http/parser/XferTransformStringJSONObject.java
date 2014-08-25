package com.levelup.http.parser;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * <p>A {@link com.levelup.http.parser.XferTransform} to turn a {@code String} into a {@link org.json.JSONObject}</p>
 *
 * <p>Use the {@link #INSTANCE}</p>
 *
 * @see com.levelup.http.parser.ResponseToJSONObject
 * @author Created by robUx4 on 20/08/2014.
 */
public final class XferTransformStringJSONObject implements XferTransform<String, JSONObject> {
	public static final XferTransformStringJSONObject INSTANCE = new XferTransformStringJSONObject();

	private XferTransformStringJSONObject() {
	}

	@Override
	public JSONObject transformData(String srcData, ImmutableHttpRequest request) throws IOException, ParserException {
		try {
			return new JSONObject(srcData);
		} catch (JSONException e) {
			throw new ParserException("Bad JSON data", e, srcData);
		} catch (NullPointerException e) {
			throw new ParserException("Invalid JSON data", e, srcData);
		}
	}
}
