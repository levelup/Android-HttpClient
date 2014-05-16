package com.levelup.http;

import android.net.Uri;

/**
 * Basic HTTP DELETE request to use with {@link HttpClient}, using {@link HttpPostParameters} parameters
 * 
 * @author Steve Lhomme
 */
public class HttpRequestDelete extends HttpRequestPost {
	private final static String HTTP_METHOD = "DELETE";

	public HttpRequestDelete(String url, HttpPostParameters httpParams) {
		super(url, httpParams);
	}

	public HttpRequestDelete(Uri uri, HttpPostParameters httpParams) {
		super(uri, httpParams);
	}

	@Override
	public final String getHttpMethod() {
		return HTTP_METHOD;
	}
}
