package com.levelup.http;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.net.Uri;

public class HttpParamsGet implements HttpGetParameters {
	private final ArrayList<NameValuePair> mParams;

	public HttpParamsGet(int capacity) {
		mParams = new ArrayList<NameValuePair>(capacity);
	}

	public HttpParamsGet() {
		mParams = new ArrayList<NameValuePair>();
	}

	@Override
	public void add(String name, String value) {
		mParams.add(new BasicNameValuePair(name, value));
	}

	@Override
	public void add(String name, boolean b) {
		add(name, String.valueOf(b));
	}

	@Override
	public void add(String name, int i) {
		add(name, String.valueOf(i));
	}

	@Override
	public void add(String name, long l) {
		add(name, String.valueOf(l));
	}

	@Override
	public void addUriParameters(Uri.Builder uriBuilder) {
		for (NameValuePair param : mParams) {
			uriBuilder.appendQueryParameter(param.getName(), param.getValue());
		}
	}
}
