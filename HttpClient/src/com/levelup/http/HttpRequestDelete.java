package com.levelup.http;

import android.net.Uri;

/**
 * Basic HTTP DELETE request to use with {@link HttpClient}, using {@link HttpBodyParameters} parameters
 * 
 * @author Steve Lhomme
 */
public class HttpRequestDelete<T> extends HttpRequestPost<T> {
	private final static String HTTP_METHOD = "DELETE";

	public HttpRequestDelete(String url, HttpBodyParameters httpParams, InputStreamParser<T> streamParser) {
		super(url, httpParams, streamParser);
	}

	public HttpRequestDelete(Uri uri, HttpBodyParameters httpParams, InputStreamParser<T> streamParser) {
		super(uri, httpParams, streamParser);
	}

	@Override
	public final String getHttpMethod() {
		return HTTP_METHOD;
	}
}
