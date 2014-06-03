package com.levelup.http;

import android.net.Uri;

/**
 * Basic HTTP GET request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 * @see BaseHttpRequest for a more complete API
 */
public class HttpRequestGet<T> extends BaseHttpRequest<T> {
	public static class Builder<T> extends BaseHttpRequest.Builder<T> {
		public HttpRequestGet<T> build() {
			return new HttpRequestGet<T>(this);
		}
	}

	public HttpRequestGet(String baseUrl, HttpUriParameters uriParams) {
		this((Builder<T>) new Builder<T>().setUrl(baseUrl, uriParams));
	}

	public HttpRequestGet(Uri baseUri, HttpUriParameters uriParams) {
		this((Builder<T>) new Builder<T>().setUrl(baseUri.toString(), uriParams));
	}

	public HttpRequestGet(String url) {
		this((Builder<T>) new Builder<T>().setUrl(url));
	}

	protected HttpRequestGet(Builder<T> builder) {
		super(builder);
	}
}
