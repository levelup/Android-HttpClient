package co.tophe.body;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import co.tophe.HttpRequestInfo;
import co.tophe.UploadProgressListener;


/**
 * HTTP body class that consists of a JSON data passed as a String 
 */
public class HttpBodyJSON implements HttpBodyParameters {

	protected final JsonElement jsonElement;

	/**
	 * Constructor with the JSONObject data to set in the POST body, the {@code org.json} way
	 */
	public HttpBodyJSON(JSONObject value) {
		this(orgToGson(value));
	}

	/**
	 * Constructor with the JSONArray data to set in the POST body, the {@code org.json} way
	 */
	public HttpBodyJSON(JSONArray value) {
		this(orgToGson(value));
	}

	/**
	 * Constructor with the JSON data to set in the POST body
	 */
	public HttpBodyJSON(JsonElement value) {
		this.jsonElement = value;
	}

	/**
	 * Copy constructor, the internal JsonObject is not cloned, so any change to the original object will change this instance too
	 * @param copy body to copy parameters from
	 */
	public HttpBodyJSON(HttpBodyJSON copy) {
		this(copy.jsonElement);
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
		output.write(jsonElement.toString().getBytes());
	}

	@Override
	public String getContentType() {
		return "application/json; charset=UTF-8";
	}

	@Override
	public long getContentLength() {
		return jsonElement.toString().getBytes().length;
	}

	public final JsonElement getJsonElement() {
		return jsonElement;
	}

	private static JsonElement orgToGson(JSONObject value) {
		JsonParser parser = new JsonParser();
		return parser.parse(value.toString());
	}

	private static JsonElement orgToGson(JSONArray value) {
		JsonParser parser = new JsonParser();
		return parser.parse(value.toString());
	}
}
