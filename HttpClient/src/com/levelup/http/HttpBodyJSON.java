package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.gson.GsonBody;


/**
 * HTTP body class that consists of a JSON data passed as a String 
 */
public class HttpBodyJSON implements HttpBodyParameters {

	private final JsonObject jsonObject;

	/**
	 * Constructor with the JSON data to set in the POST body, the {@code org.json} way
	 */
	public HttpBodyJSON(JSONObject value) {
		this(orgToGson(value));
	}
	
	/**
	 * Constructor with the JSON data to set in the POST body
	 */
	public HttpBodyJSON(JsonObject value) {
		this.jsonObject = value;
	}
	
	@Override
	public void add(String name, String value) {
		throw new IllegalAccessError();
	}

	@Override
	public void add(String name, boolean value) {
		throw new IllegalAccessError();
	}

	@Override
	public void add(String name, int value) {
		throw new IllegalAccessError();
	}

	@Override
	public void add(String name, long value) {
		throw new IllegalAccessError();
	}

	@Override
	public void settleHttpHeaders(BaseHttpRequest<?> request) {
		request.setHeader(HTTP.CONTENT_TYPE, GsonBody.CONTENT_TYPE);
	}

	@Override
	public void setOuputData(Builders.Any.B requestBuilder) {
		requestBuilder.setJsonObjectBody(jsonObject);
	}

	@Override
	public void writeBodyTo(OutputStream output, BaseHttpRequest<?> request, UploadProgressListener progressListener) throws IOException {
		throw new IllegalAccessError();
	}

	private static JsonObject orgToGson(JSONObject value) {
		JsonParser parser = new JsonParser();
		JsonObject o = (JsonObject) parser.parse(value.toString());
		return o;
	}
}
