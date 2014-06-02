package com.levelup.http;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Basic HTTP GET request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 */
public class HttpRequestGet<T> extends BaseHttpRequest<T> {
	private static final String HTTP_METHOD = "GET";

	public static class Builder<T> extends BaseHttpRequest.Builder<T> {

		public Builder() {
			super.setHttpMethod(HTTP_METHOD);
		}

		public Builder<T> setHttpMethod(String httpMethod) {
			if (!TextUtils.equals(httpMethod, "GET") && !TextUtils.equals(httpMethod, "HEAD"))
				throw new IllegalArgumentException("invalid HTTP method:"+httpMethod);
			super.setHttpMethod(httpMethod);
			return this;
		}

		public HttpRequestGet<T> build() {
			return new HttpRequestGet<T>(this);
		}
	}

	public HttpRequestGet(String baseUrl, HttpUriParameters httpParams, InputStreamParser<T> streamParser) {
		super(addUriParams(baseUrl, httpParams), HTTP_METHOD, streamParser);
	}

	public HttpRequestGet(Uri baseUri, HttpUriParameters httpParams, InputStreamParser<T> streamParser) {
		super(addUriParams(baseUri, httpParams), HTTP_METHOD, streamParser);
	}

	public HttpRequestGet(String url, InputStreamParser<T> streamParser) {
		super(url, HTTP_METHOD, streamParser);
	}

	public HttpRequestGet(String url) {
		this(url, null);
	}

	protected HttpRequestGet(Builder<T> builder) {
		super(builder);
	}

	static Uri addUriParams(String baseUrl, HttpUriParameters httpParams) {
		return addUriParams(Uri.parse(baseUrl), httpParams);
	}

	private static Uri addUriParams(Uri uri, HttpUriParameters httpParams) {
		if (null==httpParams)
			return uri;
		Uri.Builder uriBuilder = uri.buildUpon();
		httpParams.addUriParameters(uriBuilder);
		return uriBuilder.build();
	}

}
