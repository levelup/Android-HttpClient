package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

/**
 * HTTP POST parameter that are sent as {@link form-urlencoded}
 */
public class HttpParamsUrlEncoded implements HttpPostParameters {

	private final ArrayList<NameValuePair> mParams;

	/**
	 * Basic constructor
	 */
	public HttpParamsUrlEncoded() {
		mParams = new ArrayList<NameValuePair>();
    }

	/**
	 * Constructor with an initial amount of parameters to hold
	 * @param capacity amount of parameters the object will get
	 */
	public HttpParamsUrlEncoded(int capacity) {
		mParams = new ArrayList<NameValuePair>(capacity);
    }

	@Override
    public void settleHttpHeaders(HttpRequestPost request) {
		request.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

		String encoded = URLEncodedUtils.format(mParams, "UTF-8");
		request.setHeader(HTTP.CONTENT_LEN, Integer.toString(encoded.getBytes().length));
	}

	@Override
	public void writeBodyTo(OutputStream output) throws UnsupportedEncodingException, IOException {
		String encoded = URLEncodedUtils.format(mParams, "UTF-8");
		output.write(encoded.getBytes());
	}

	@Override
	public void add(String name, String value) {
		mParams.add(new BasicNameValuePair(name, value));
	}

	@Override
	public void add(String name, boolean b) {
		add(name, String.valueOf(b));
	}

	@Override
	public void add(String name, int i) {
		add(name, Integer.toString(i));
	}

	@Override
	public void add(String name, long l) {
		add(name, Long.toString(l));
	}
}
