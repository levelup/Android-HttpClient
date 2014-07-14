package com.levelup.http.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.os.Build;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;

/**
 * Basic HTTP request to be passed to {@link com.levelup.http.HttpClient}
 *
 * @param <T> type of the data read from the HTTP response
 * @see com.levelup.http.HttpRequestGet for a more simple API
 * @see com.levelup.http.HttpRequestPost for a more simple POST API
 */
public class HttpRequestUrlConnection<T> extends BaseHttpRequestImpl<T> {
	final HttpURLConnection urlConnection;

	public HttpRequestUrlConnection(BaseHttpRequest.AbstractBuilder<T,?> builder) {
		super(builder);

		try {
			this.urlConnection = (HttpURLConnection) new URL(getUri().toString()).openConnection();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Bad uri: " + getUri(), e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean isStreaming() {
		return true;
	}

	@SuppressLint("NewApi")
	@Override
	public void settleHttpHeaders() throws HttpException {
		try {
			urlConnection.setRequestMethod(getHttpMethod());
		} catch (ProtocolException e) {
			HttpClient.forwardResponseException(this, e);
		}

		final long contentLength;
		if (null != bodyParams) {
			setHeader(HTTP.CONTENT_TYPE, bodyParams.getContentType());
			contentLength = bodyParams.getContentLength();
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
		} else {
			contentLength = 0L;
		}
		setHeader(HTTP.CONTENT_LEN, Long.toString(contentLength));

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			urlConnection.setFixedLengthStreamingMode((int) contentLength);
		else
			urlConnection.setFixedLengthStreamingMode(contentLength);

		super.settleHttpHeaders();

		for (Entry<String, String> entry : mRequestSetHeaders.entrySet()) {
			urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
		}
		for (Entry<String, HashSet<String>> entry : mRequestAddHeaders.entrySet()) {
			for (String value : entry.getValue()) {
				urlConnection.addRequestProperty(entry.getKey(), value);
			}
		}

		if (null!=followRedirect) {
			urlConnection.setInstanceFollowRedirects(followRedirect);
		}

		if (null != getHttpConfig()) {
			int readTimeout = getHttpConfig().getReadTimeout(this);
			if (readTimeout>=0)
				urlConnection.setReadTimeout(readTimeout);
		}
	}

	@Override
	public final void setupBody() {
		// do nothing
	}

	@Override
	public final void doConnection() throws IOException {
		urlConnection.connect();

		if (null != bodyParams) {
			OutputStream output = urlConnection.getOutputStream();
			try {
				outputBody(output);
			} finally {
				output.close();
			}
		}
	}

	@Override
	protected InputStream getParseableErrorStream() throws IOException {
		HttpResponseUrlConnection response = (HttpResponseUrlConnection) getResponse();
		InputStream errorStream = response.getErrorStream();
		if (null == errorStream)
			errorStream = response.getInputStream();

		if (null == errorStream)
			return null;

		if ("deflate".equals(response.getContentEncoding()) && !(errorStream instanceof InflaterInputStream))
			errorStream = new InflaterInputStream(errorStream);
		if ("gzip".equals(response.getContentEncoding()) && !(errorStream instanceof GZIPInputStream))
			errorStream = new GZIPInputStream(errorStream);

		return errorStream;
	}
}
