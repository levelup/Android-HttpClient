package com.levelup.http.parser;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import com.levelup.http.DataErrorException;
import com.levelup.http.HttpResponse;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class DataTransformResponseJSONObject implements DataTransform<HttpResponse, JSONObject> {
	public static final DataTransformResponseJSONObject INSTANCE = new DataTransformResponseJSONObject();

	public DataTransformResponseJSONObject() {
	}

	@Override
	public JSONObject transform(HttpResponse response, ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException {
		InputStream inputStream = DataTransformResponseInputStream.INSTANCE.transform(response, request);
		String jsonString = DataTransformInputStreamString.INSTANCE.transform(inputStream, request);
		return DataTransformStringJSONObject.INSTANCE.transform(jsonString, request);
	}
}
