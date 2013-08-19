package com.levelup.http;

import android.net.Uri;


public class HttpRequestGet extends HttpRequest {

	public HttpRequestGet(String baseUrl, HttpGetParameters httpParams) {
		super(addUriParams(baseUrl, httpParams));
	}

	public HttpRequestGet(Uri baseUri, HttpGetParameters httpParams) {
		super(addUriParams(baseUri, httpParams));
	}

	public HttpRequestGet(String url) {
		super(url);
	}

	private static Uri addUriParams(String baseUrl, HttpGetParameters httpParams) {
		return addUriParams(Uri.parse(baseUrl), httpParams);
	}

	private static Uri addUriParams(Uri uri, HttpGetParameters httpParams) {
		if (null==httpParams)
			return uri;
		Uri.Builder uriBuilder = uri.buildUpon();
		httpParams.addUriParameters(uriBuilder);
		return uriBuilder.build();
	}

}
