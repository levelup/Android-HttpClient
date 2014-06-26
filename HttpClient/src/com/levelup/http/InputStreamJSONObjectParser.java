package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

public class InputStreamJSONObjectParser implements InputStreamParser<JSONObject> {

	public static final InputStreamJSONObjectParser instance = new InputStreamJSONObjectParser();
	
	private InputStreamJSONObjectParser() {
	}

	@Override
	public JSONObject parseInputStream(InputStream inputStream, HttpRequest request) throws IOException, ParserException {
		String srcData = InputStreamStringParser.instance.parseInputStream(inputStream, request);
		try {
			return new JSONObject(srcData);
		} catch (JSONException e) {
			throw new ParserException("Bad JSON data", e, srcData);
		} catch (NullPointerException e) {
			throw new ParserException("Invalid JSON data", e, srcData);
		}
	}
}
