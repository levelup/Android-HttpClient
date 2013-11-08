package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import android.net.Uri;

public class HttpRequestPost extends AbstractHttpRequest {
	private final HttpPostParameters httpParams;
	private UploadProgressListener mProgressListener;

	public HttpRequestPost(String url, HttpPostParameters httpParams) {
		super(url);
		this.httpParams = httpParams;
	}

	public HttpRequestPost(Uri uri, HttpPostParameters httpParams) {
		super(uri);
		this.httpParams = httpParams;
	}

	public void setProgressListener(UploadProgressListener listener) {
		this.mProgressListener = listener;
	}

	public UploadProgressListener getProgressListener() {
		return mProgressListener;
	}

	@Override
	public void setConnectionProperties(HttpURLConnection connection) throws ProtocolException {
		connection.setRequestMethod("POST");
		connection.setDoInput(true);
		connection.setDoOutput(true);

		if (null != httpParams)
			httpParams.setConnectionProperties(connection);

		super.setConnectionProperties(connection);
	}

	@Override
	public void settleHttpHeaders() throws HttpException {
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
		final UploadProgressListener listener = mProgressListener;
		if (null != listener)
			listener.onParamUploadProgress(this, null, 0);
		if (null != httpParams)
			httpParams.writeBodyTo(output, this, listener);
		if (null != listener)
			listener.onParamUploadProgress(this, null, 100);
	}

}
