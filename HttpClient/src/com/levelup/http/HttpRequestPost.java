package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import org.apache.http.protocol.HTTP;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Basic HTTP POST request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 */
public class HttpRequestPost<T> extends BaseHttpRequest<T> {
	private static final String HTTP_METHOD = "POST";

	public static class Builder<T> extends BaseHttpRequest.Builder<T> {

		public Builder() {
			super.setHttpMethod(HTTP_METHOD);
		}

		public Builder<T> setHttpMethod(String httpMethod) {
			if (TextUtils.equals(httpMethod, "GET") || TextUtils.equals(httpMethod, "HEAD"))
				throw new IllegalArgumentException("invalid HTTP method with body:"+httpMethod);
			super.setHttpMethod(httpMethod);
			return this;
		}

		private HttpBodyParameters bodyParams;
		
		public Builder<T> setHttpParams(HttpBodyParameters bodyParams) {
			this.bodyParams = bodyParams;
			return this;
		}

		public final HttpBodyParameters getHttpParams() {
			return bodyParams;
		}

		public HttpRequestPost<T> build() {
			return new HttpRequestPost<T>(this);
		}
	}

	private final HttpBodyParameters bodyParams;
	private UploadProgressListener mProgressListener;

	public HttpRequestPost(String url, HttpBodyParameters bodyParams, InputStreamParser<T> streamParser) {
		super(url, HTTP_METHOD, streamParser);
		this.bodyParams = bodyParams;
	}

	public HttpRequestPost(Uri uri, HttpBodyParameters bodyParams, InputStreamParser<T> streamParser) {
		super(uri, HTTP_METHOD, streamParser);
		this.bodyParams = bodyParams;
	}

	protected HttpRequestPost(Builder<T> builder) {
		super(builder);
		this.bodyParams = builder.getHttpParams();
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

		if (null != bodyParams)
			bodyParams.setConnectionProperties(connection);

		super.setConnectionProperties(connection);
	}

	@Override
	public void settleHttpHeaders() throws HttpException {
		if (null != bodyParams)
			bodyParams.settleHttpHeaders(this);
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
		if (null != bodyParams)
			bodyParams.writeBodyTo(output, this, listener);
		if (null != listener)
			listener.onParamUploadProgress(this, null, 100);
	}

}
