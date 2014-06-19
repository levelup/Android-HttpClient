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
	public JSONArray parseInputStream(InputStream inputStream, HttpRequest request) throws IOException, ParserException {
		String srcData = InputStreamStringParser.instance.parseInputStream(inputStream, request);
		try {
			return new JSONArray(srcData);
		} catch (JSONException e) {
			throw new ParserException("Bad JSON data", request.newException().setErrorMessage("Bad JSON data "+srcData+' '+e.getMessage()).setCause(e).setErrorCode(HttpException.ERROR_JSON).build());
		} catch (NullPointerException e) {
			throw new ParserException("Invalid JSON data", request.newException().setErrorMessage("Invalid JSON data "+srcData).setCause(e).build());
		}
	}
}
