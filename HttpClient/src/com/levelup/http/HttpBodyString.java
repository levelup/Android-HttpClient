package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import org.apache.http.protocol.HTTP;

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
	public void settleHttpHeaders(BaseHttpRequest<?> request) {
		request.setHeader(HTTP.CONTENT_TYPE, StringBody.CONTENT_TYPE);
	}
	
	@Override
	public void setConnectionProperties(HttpURLConnection connection) {
	}

	@Override
	public void setOuputData(Builders.Any.B requestBuilder) {
		requestBuilder.setStringBody(value);
	}
	
	@Override
	public void writeBodyTo(OutputStream output, BaseHttpRequest<?> request, UploadProgressListener progressListener) throws IOException {
		throw new IllegalAccessError();
	}
}
