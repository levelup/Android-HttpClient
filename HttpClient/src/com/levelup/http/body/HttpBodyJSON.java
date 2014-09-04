package com.levelup.http.body;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.levelup.http.HttpRequestInfo;
import com.levelup.http.UploadProgressListener;


/**
 * HTTP body class that consists of a JSON data passed as a String 
 */
public class HttpBodyJSON implements HttpBodyParameters {

	protected final JsonObject jsonObject;

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

	/**
	 * Copy constructor, the internal JsonObject is not cloned, so any change to the original object will change this instance too
	 * @param copy body to copy parameters from
	 */
	public HttpBodyJSON(HttpBodyJSON copy) {
		this(copy.jsonObject);
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
	public void writeBodyTo(OutputStream output, HttpRequestInfo request, UploadProgressListener progressListener) throws IOException {
		output.write(jsonObject.toString().getBytes());
	}

	@Override
	public String getContentType() {
		return "application/json; charset=UTF-8";
	}

	@Override
	public long getContentLength() {
		return jsonObject.toString().getBytes().length;
	}

	public final JsonObject getJsonObject() {
		return jsonObject;
	}

	private static JsonObject orgToGson(JSONObject value) {
		JsonParser parser = new JsonParser();
		JsonObject o = (JsonObject) parser.parse(value.toString());
		return o;
	}
}
