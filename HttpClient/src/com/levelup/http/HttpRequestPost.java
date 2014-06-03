package com.levelup.http;

import android.net.Uri;

/**
 * Basic HTTP POST request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 * @see BaseHttpRequest for a more complete API
 */
public class HttpRequestPost<T> extends BaseHttpRequest<T> {
	public static class Builder<T> extends BaseHttpRequest.Builder<T> {
		public Builder() {
			setHttpMethod("POST");
		}

		public HttpRequestPost<T> build() {
			return new HttpRequestPost<T>(this);
		}
	}

	public HttpRequestPost(String url, HttpBodyParameters bodyParams) {
		this((Builder<T>) new Builder<T>().setBody(bodyParams).setUrl(url));
	}

	public HttpRequestPost(Uri uri, HttpBodyParameters bodyParams) {
		this((Builder<T>) new Builder<T>().setBody(bodyParams).setUri(uri));
	}

	protected HttpRequestPost(Builder<T> builder) {
		super(builder);
	}
}
