package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.ion.builder.Builders;


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
	public void setOutputData(Builders.Any.B requestBuilder) {
		requestBuilder.setJsonObjectBody(jsonObject);
	}

	@Override
	public void writeBodyTo(OutputStream output, HttpRequest request, UploadProgressListener progressListener) throws IOException {
		output.write(jsonObject.toString().getBytes());
	}

	@Override
	public String getContentType() {
		return JSONObjectBody.CONTENT_TYPE;
	}

	@Override
	public long getContentLength() {
		return jsonObject.toString().getBytes().length;
	}

	private static JsonObject orgToGson(JSONObject value) {
		JsonParser parser = new JsonParser();
		JsonObject o = (JsonObject) parser.parse(value.toString());
		return o;
	}
}
