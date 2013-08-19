package com.levelup.http;

import org.json.JSONObject;


/**
 * HTTP POST parameter that consists of a JSON data passed as a String 
 */
public class HttpParamsJSON extends HttpParamsPostString {

	private static final String JSON_TYPE = "application/json; charset=UTF-8";

	/**
	 * Constructor with the JSON data to set in the POST body
	 */
	public HttpParamsJSON(JSONObject value) {
		super(value.toString(), JSON_TYPE);
	}
}
