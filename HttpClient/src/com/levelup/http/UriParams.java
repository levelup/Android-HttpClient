package com.levelup.http;

import java.util.ArrayList;

import android.net.Uri;
import android.util.Pair;

public class UriParams implements HttpUriParameters {
	private final ArrayList<Pair<String,String>> mParams;

	public UriParams(int capacity) {
		mParams = new ArrayList<Pair<String,String>>(capacity);
	}

	public UriParams() {
		mParams = new ArrayList<Pair<String,String>>();
	}

	@Override
	public void add(String name, String value) {
		mParams.add(new Pair<String,String>(name, value));
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
		for (Pair<String,String> param : mParams) {
			uriBuilder.appendQueryParameter(param.first, param.second);
		}
	}
}
