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
	public JSONObject parseInputStream(InputStream inputStream, HttpRequest request) throws IOException {
		String srcData = InputStreamStringParser.instance.parseInputStream(inputStream, request);
		try {
			return new JSONObject(srcData);
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
