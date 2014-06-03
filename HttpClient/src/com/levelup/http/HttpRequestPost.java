package com.levelup.http;

import android.net.Uri;

/**
 * Basic HTTP POST request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 */
public class HttpRequestPost<T> extends BaseHttpRequest<T> {
	public static class Builder<T> extends BaseHttpRequest.Builder<T> {

		public Builder(HttpBodyParameters bodyParams) {
			super("POST", bodyParams);
		}

		public HttpRequestPost<T> build() {
			return new HttpRequestPost<T>(this);
		}
	}

	public HttpRequestPost(String url, HttpBodyParameters bodyParams, InputStreamParser<T> streamParser) {
		this((Builder<T>) new Builder<T>(bodyParams).setUrl(url).setStreamParser(streamParser));
	}

	public HttpRequestPost(Uri uri, HttpBodyParameters bodyParams, InputStreamParser<T> streamParser) {
		this((Builder<T>) new Builder<T>(bodyParams).setUri(uri).setStreamParser(streamParser));
	}

	protected HttpRequestPost(Builder<T> builder) {
		super(builder);
	}
}
