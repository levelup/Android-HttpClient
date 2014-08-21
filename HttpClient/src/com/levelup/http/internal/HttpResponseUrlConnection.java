package com.levelup.http.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import com.levelup.http.HttpResponse;

/**
 * Created by Steve Lhomme on 09/07/2014.
 */
public class HttpResponseUrlConnection implements HttpResponse {

	private final HttpEngineUrlConnection request;
	private final HttpURLConnection response;
	private InputStream inputStream;
	private InputStream errorStream;

	public HttpResponseUrlConnection(HttpEngineUrlConnection request) {
		if (null == request) throw new NullPointerException();
		if (null == request.urlConnection) throw new NullPointerException();
		this.response = request.urlConnection;
		this.request = request;
	}

	@Override
	public String getContentType() {
		return response.getContentType();
	}

	@Override
	public int getResponseCode() throws IOException {
		return response.getResponseCode();
	}

	@Override
	public Map<String, List<String>> getHeaderFields() {
		return response.getHeaderFields();
	}

	@Override
	public String getHeaderField(String name) {
		return response.getHeaderField(name);
	}

	@Override
	public int getContentLength() {
		return response.getContentLength();
	}

	@Override
	public String getResponseMessage() throws IOException {
		return response.getResponseMessage();
	}

	@Override
	public String getContentEncoding() {
		return response.getContentEncoding();
	}

	@Override
	public void disconnect() {
		response.disconnect();
	}

	private InputStream getDecompressedStream(InputStream stream) throws IOException {
		if (null != stream) {
			if ("deflate".equals(getContentEncoding()) && !(stream instanceof InflaterInputStream))
				stream = new InflaterInputStream(stream);
			if ("gzip".equals(getContentEncoding()) && !(stream instanceof GZIPInputStream))
				stream = new GZIPInputStream(stream);
		}
		return stream;
	}

	public InputStream getErrorStream() throws IOException {
		if (null == errorStream) {
			errorStream = getDecompressedStream(response.getErrorStream());
		}
		return errorStream;
	}

	public InputStream getInputStream() throws IOException {
		if (null == inputStream) {
			inputStream = getDecompressedStream(response.getInputStream());
		}
		return inputStream;
	}

	public InputStream getContentStream() throws IOException {
		if (null != inputStream)
			return inputStream;
		if (null != errorStream)
			return errorStream;

		InputStream result = getInputStream();
		if (null == result)
			result = getErrorStream();
		return result;
	}
}
