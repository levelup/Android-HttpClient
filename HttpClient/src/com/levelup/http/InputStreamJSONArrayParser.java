package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;

public class InputStreamJSONArrayParser implements InputStreamParser<JSONArray> {

	public static final InputStreamJSONArrayParser instance = new InputStreamJSONArrayParser();
	
	private InputStreamJSONArrayParser() {
	}

	@Override
	public JSONArray parseInputStream(InputStream inputStream, HttpRequest request) throws IOException {
		String srcData = InputStreamStringParser.instance.parseInputStream(inputStream, request);
		try {
			return new JSONArray(srcData);
		} catch (JSONException e) {
			IOException forward = new IOException("Bad JSON data "+srcData);
			forward.initCause(e);
			throw forward;
		} catch (NullPointerException e) {
			IOException forward = new IOException("Invalid JSON data "+srcData);
			forward.initCause(e);
			throw forward;
		}
	}
}
