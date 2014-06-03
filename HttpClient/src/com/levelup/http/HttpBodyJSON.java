package com.levelup.http;

import org.json.JSONObject;


/**
 * HTTP body class that consists of a JSON data passed as a String 
 */
public class HttpBodyJSON extends HttpBodyString {

	private static final String JSON_TYPE = "application/json; charset=UTF-8";

	/**
	 * Constructor with the JSON data to set in the POST body
	 */
	public HttpBodyJSON(JSONObject value) {
		super(value.toString(), JSON_TYPE);
	}
}
