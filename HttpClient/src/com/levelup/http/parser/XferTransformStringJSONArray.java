package com.levelup.http.parser;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * <p>A {@link com.levelup.http.parser.XferTransform} to turn a {@code String} into a {@link org.json.JSONArray}</p>
 *
 * <p>Use the {@link #INSTANCE}</p>
 *
 * @see com.levelup.http.parser.ResponseToJSONArray
 * @author Created by robUx4 on 20/08/2014.
 */
public final class XferTransformStringJSONArray implements XferTransform<String, JSONArray> {
	public static final XferTransformStringJSONArray INSTANCE = new XferTransformStringJSONArray();

	private XferTransformStringJSONArray() {
	}

	@Override
	public JSONArray transformData(String srcData, ImmutableHttpRequest request) throws IOException, ParserException {
		try {
			return new JSONArray(srcData);
		} catch (JSONException e) {
			throw new ParserException("Bad JSON data", e, srcData);
		} catch (NullPointerException e) {
			throw new ParserException("Invalid JSON data", e, srcData);
		}
	}
}
