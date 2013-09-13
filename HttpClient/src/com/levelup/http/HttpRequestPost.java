package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import android.net.Uri;

public class HttpRequestPost extends AbstractHttpRequest {
	private final HttpPostParameters httpParams;

	public HttpRequestPost(String url, HttpPostParameters httpParams) {
		super(url);
		this.httpParams = httpParams;
	}

	public HttpRequestPost(Uri uri, HttpPostParameters httpParams) {
		super(uri);
		this.httpParams = httpParams;
	}

	@Override
	public void setConnectionProperties(HttpURLConnection connection) throws ProtocolException {
		connection.setRequestMethod("POST");
		connection.setDoInput(true);
		connection.setDoOutput(true);

		super.setConnectionProperties(connection);
	}
	
	@Override
	public void settleHttpHeaders() {
		if (null != httpParams)
			httpParams.settleHttpHeaders(this);

		super.settleHttpHeaders();
	}

	/**
	 * This is {@code final} as {@link #outputBody(OutputStream)} should be the one extended
	 */
	public final void outputBody(HttpURLConnection connection) throws IOException {
		OutputStream output = null;
		try {
			output = connection.getOutputStream();
			outputBody(output);
		} finally {
			if (null != output) {
				output.close();
			}
		}
	}

	public void outputBody(OutputStream output) throws IOException {
		if (null != httpParams)
			httpParams.writeBodyTo(output);
	}

}
