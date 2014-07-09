package com.levelup.http;

import android.net.Uri;

/**
 * An Http GET request that doesn't follow redirections
 *
 * @author Steve Lhomme
 */
public class HttpRequestGetNoRedirect extends BaseHttpRequest<Void> {

	public static class Builder extends BaseHttpRequest.Builder<Void> {
		public HttpRequestGetNoRedirect build() {
			return new HttpRequestGetNoRedirect(this);
		}
	}
	
	public HttpRequestGetNoRedirect(String baseUrl) {
		this((Builder) new Builder().setUrl(baseUrl));
	}

	public HttpRequestGetNoRedirect(String baseUrl, HttpUriParameters uriParams) {
		this((Builder) new Builder().setUrl(baseUrl, uriParams));
	}

	public HttpRequestGetNoRedirect(Uri baseUri, HttpUriParameters uriParams) {
		this((Builder) new Builder().setUrl(baseUri.toString(), uriParams));
	}

	public HttpRequestGetNoRedirect(Builder builder) {
		super(builder);
		requestBuilder.followRedirect(false);
	}
}