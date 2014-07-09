package com.levelup.http.internal;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.protocol.HTTP;

import android.text.TextUtils;

import com.koushikdutta.ion.Response;
import com.levelup.http.HttpResponse;

/**
 * Created by Steve Lhomme on 09/07/2014.
 */
public class IonResponse implements HttpResponse {
	private final Response<?> response;

	public IonResponse(Response<?> response) {
		this.response = response;
	}

	@Override
	public String getContentType() {
		return response.getHeaders().get(HTTP.CONTENT_TYPE);
	}

	@Override
	public int getResponseCode() throws IOException {
		return response.getHeaders().getResponseCode();
	}

	@Override
	public Map<String, List<String>> getRequestProperties() {
		return response.getRequest().getHeaders().getHeaders().toMultimap();
	}

	@Override
	public Map<String, List<String>> getHeaderFields() {
		return response.getHeaders().toMultimap();
	}

	@Override
	public String getHeaderField(String name) {
		return response.getHeaders().get(name);
	}

	@Override
	public int getContentLength() {
		String contentLength = getHeaderField(HTTP.CONTENT_LEN);
		if (TextUtils.isEmpty(contentLength))
			return -1;
		return Integer.parseInt(contentLength);
	}

	@Override
	public String getResponseMessage() throws IOException {
		return response.getHeaders().getResponseMessage();
	}

	@Override
	public String getContentEncoding() {
		return getHeaderField(HTTP.CONTENT_ENCODING);
	}

	@Override
	public void disconnect() {
		// TODO see if we can cancel a Ion response while it's processing
	}

	public Object getResult() {
		return response.getResult();
	}
}
