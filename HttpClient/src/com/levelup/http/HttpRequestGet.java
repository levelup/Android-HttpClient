package com.levelup.http;

import android.net.Uri;

/**
 * Basic HTTP GET request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 */
public class HttpRequestGet extends BaseHttpRequest {
	private static final String HTTP_METHOD = "GET";

	public HttpRequestGet(String baseUrl, HttpGetParameters httpParams) {
		super(addUriParams(baseUrl, httpParams), HTTP_METHOD);
	}

	public HttpRequestGet(Uri baseUri, HttpGetParameters httpParams) {
		super(addUriParams(baseUri, httpParams), HTTP_METHOD);
	}

	public HttpRequestGet(String url) {
		super(url, HTTP_METHOD);
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
