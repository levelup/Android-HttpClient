package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

/**
 * HTTP body class with data sent as {@code form-urlencoded}
 */
public class HttpBodyUrlEncoded implements HttpBodyParameters {

	protected final ArrayList<NameValuePair> mParams;
	private byte[] encodedParams;
	private static final String CONTENT_TYPE = URLEncodedUtils.CONTENT_TYPE + "; charset=utf-8";

	/**
	 * Basic constructor
	 */
	public HttpBodyUrlEncoded() {
		mParams = new ArrayList<NameValuePair>();
	}

	/**
	 * Constructor with an initial amount of parameters to hold
	 * @param capacity amount of parameters the object will get
	 */
	public HttpBodyUrlEncoded(int capacity) {
		mParams = new ArrayList<NameValuePair>(capacity);
	}

	/**
	 * Copy constructor
	 * @param copy body to copy parameters from
	 */
	public HttpBodyUrlEncoded(HttpBodyUrlEncoded copy) {
		this.mParams = new ArrayList<NameValuePair>(copy.mParams);
	}

	private byte[] getEncodedParams() {
		if (null==encodedParams) {
			encodedParams = URLEncodedUtils.format(mParams, "UTF-8").replace("*", "%2A").getBytes();
			mParams.clear();
		}
		return encodedParams;
	}

	@Override
	public String getContentType() {
		return "application/x-www-form-urlencoded";
	}

	@Override
	public long getContentLength() {
		return getEncodedParams().length;
	}

	@Override
	public void writeBodyTo(OutputStream output, HttpRequestInfo request, UploadProgressListener progressListener) throws IOException {
		output.write(getEncodedParams());
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
