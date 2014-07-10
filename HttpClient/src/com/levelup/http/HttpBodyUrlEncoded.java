package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.koushikdutta.async.http.body.UrlEncodedFormBody;
import com.koushikdutta.ion.builder.Builders;

/**
 * HTTP body class with data sent as {@code form-urlencoded}
 */
public class HttpBodyUrlEncoded implements HttpBodyParameters {

	private final ArrayList<NameValuePair> mParams;
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

	private byte[] getEncodedParams() {
		if (null==encodedParams) {
			encodedParams = URLEncodedUtils.format(mParams, "UTF-8").replace("*", "%2A").getBytes();
			mParams.clear();
		}
		return encodedParams;
	}

	@Override
	public String getContentType() {
		return UrlEncodedFormBody.CONTENT_TYPE;
	}

	@Override
	public long getContentLength() {
		return getEncodedParams().length;
	}

	@Override
	public void setOutputData(Builders.Any.B requestBuilder) {
		for (NameValuePair param : mParams) {
			requestBuilder.setBodyParameter(param.getName(), param.getValue());
		}
	}
	
	@Override
	public void writeBodyTo(OutputStream output, BaseHttpRequest<?> request, UploadProgressListener progressListener) throws IOException {
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
