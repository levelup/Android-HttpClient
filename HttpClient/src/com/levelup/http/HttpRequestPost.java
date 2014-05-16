package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import org.apache.http.protocol.HTTP;

import android.net.Uri;

/**
 * Basic HTTP POST request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 */
public class HttpRequestPost extends BaseHttpRequest {
	private static final String HTTP_METHOD = "POST";

	private final HttpPostParameters httpParams;
	private UploadProgressListener mProgressListener;

	public HttpRequestPost(String url, HttpPostParameters httpParams) {
		super(url, HTTP_METHOD);
		this.httpParams = httpParams;
	}

	public HttpRequestPost(Uri uri, HttpPostParameters httpParams) {
		super(uri, HTTP_METHOD);
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
		else
			setHeader(HTTP.CONTENT_LEN, "0");

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
