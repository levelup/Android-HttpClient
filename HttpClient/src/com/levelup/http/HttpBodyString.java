package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;

import com.koushikdutta.async.http.body.StringBody;
import com.koushikdutta.ion.builder.Builders;


/**
 * HTTP body class that consists of a String data and its Content-Type 
 */
public class HttpBodyString implements HttpBodyParameters {

	private final String value;

	public HttpBodyString(String value) {
		this.value = value;
	}

	/**
	 * Do not use, extra parameters in the URL are not supported
	 * @throws IllegalAccessError
	 */
	@Deprecated
	@Override
	public void add(String name, String value) {
		throw new IllegalAccessError();
	}

	/**
	 * Do not use, extra parameters in the URL are not supported
	 * @throws IllegalAccessError
	 */
	@Deprecated
	@Override
	public void add(String name, boolean b) {
		throw new IllegalAccessError();
	}

	/**
	 * Do not use, extra parameters in the URL are not supported
	 * @throws IllegalAccessError
	 */
	@Deprecated
	@Override
	public void add(String name, int value) {
		throw new IllegalAccessError();
	}

	/**
	 * Do not use, extra parameters in the URL are not supported
	 * @throws IllegalAccessError
	 */
	@Deprecated
	@Override
	public void add(String name, long value) {
		throw new IllegalAccessError();
	}

	@Override
	public void setOutputData(Builders.Any.B requestBuilder) {
		requestBuilder.setStringBody(value);
	}

	@Override
	public String getContentType() {
		return StringBody.CONTENT_TYPE;
	}

	@Override
	public long getContentLength() {
		return value.getBytes().length;
	}

	@Override
	public void writeBodyTo(OutputStream output, HttpRequest request, UploadProgressListener progressListener) throws IOException {
		output.write(value.getBytes());
	}
}
