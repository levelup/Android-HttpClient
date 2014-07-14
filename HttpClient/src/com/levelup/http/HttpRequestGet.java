package com.levelup.http;

import android.net.Uri;

/**
 * Basic HTTP GET request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 * @see BaseHttpRequest for a more complete API
 */
public class HttpRequestGet<T> extends BaseHttpRequest<T> {
	public static abstract class AbstractBuilder<T,R extends HttpRequestGet<T>> extends BaseHttpRequest.AbstractBuilder<T,R> {
	}

	public final static class Builder<T> extends AbstractBuilder<T,HttpRequestGet<T>> {
		@Override
		protected final HttpRequestGet<T> build(HttpRequestImpl<T> impl) {
			return new HttpRequestGet<T>(impl);
		}
	}

	public HttpRequestGet(String baseUrl, HttpUriParameters uriParams) {
		this(new Builder<T>().setUrl(baseUrl, uriParams).buildImpl());
	}

	public HttpRequestGet(Uri baseUri, HttpUriParameters uriParams) {
		this(new Builder<T>().setUrl(baseUri.toString(), uriParams).buildImpl());
	}

	public HttpRequestGet(String url) {
		this(new Builder<T>().setUrl(url).buildImpl());
	}

	protected HttpRequestGet(HttpRequestImpl<T> impl) {
		super(impl);
	}
}
