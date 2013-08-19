package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import android.net.Uri;

public class HttpRequestPost extends HttpRequest {
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
	public void setRequestProperties(HttpURLConnection connection) throws ProtocolException {
		connection.setRequestMethod("POST");
		connection.setDoInput(true);
		connection.setDoOutput(true);

		if (null != httpParams)
			httpParams.setRequestProperties(connection);

		super.setRequestProperties(connection);
	}

	public void outputBody(HttpURLConnection connection) throws IOException {
		OutputStream output = null;
		try {
			output = connection.getOutputStream();
			outputBody(output);
		} finally {
			if (null != output)
				output.flush();
		}
	}

	@Override
	public void outputBody(OutputStream output) throws IOException {
		if (null != httpParams)
			httpParams.writeBodyTo(output);
	}

}
