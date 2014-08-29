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
		protected final HttpRequestGet<T> build(HttpEngine<T,?> impl) {
			return new HttpRequestGet<T>(impl);
		}
	}

	public HttpRequestGet(String baseUrl, HttpUriParameters uriParams, ResponseHandler<T> responseHandler) {
		this(new Builder<T>().setUrl(baseUrl, uriParams).setResponseParser(responseHandler).buildImpl());
	}

	public HttpRequestGet(Uri baseUri, HttpUriParameters uriParams, ResponseHandler<T> responseHandler) {
		this(new Builder<T>().setUrl(baseUri.toString(), uriParams).setResponseParser(responseHandler).buildImpl());
	}

	public HttpRequestGet(String url, ResponseHandler<T> responseHandler) {
		this(new Builder<T>().setUrl(url).setResponseParser(responseHandler).buildImpl());
	}

	protected HttpRequestGet(HttpEngine<T,?> impl) {
		super(impl);
	}
}
