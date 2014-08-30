package com.levelup.http;

import android.net.Uri;

/**
 * Basic HTTP GET request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 * @see BaseHttpRequest for a more complete API
 */
public class HttpRequestGet<T> extends BaseHttpRequest<T> {
	public static abstract class AbstractBuilder<T, REQ extends HttpRequestGet<T>, BUILDER extends AbstractBuilder<T,REQ,BUILDER>> extends BaseHttpRequest.AbstractBuilder<T,REQ,BUILDER> {
	}

	public static abstract class ChildBuilder<T, REQ extends HttpRequestGet<T>> extends AbstractBuilder<T, REQ, ChildBuilder<T, REQ>> {
	}

	public final static class Builder<T> extends AbstractBuilder<T,HttpRequestGet<T>,Builder<T>> {
		@Override
		protected HttpRequestGet<T> build(Builder<T> builder) {
			return new HttpRequestGet<T>(builder);
		}
	}

	public HttpRequestGet(String baseUrl, HttpUriParameters uriParams, ResponseHandler<T> responseHandler) {
		this(new Builder<T>().setUrl(baseUrl, uriParams).setResponseParser(responseHandler));
	}

	public HttpRequestGet(Uri baseUri, HttpUriParameters uriParams, ResponseHandler<T> responseHandler) {
		this(new Builder<T>().setUrl(baseUri.toString(), uriParams).setResponseParser(responseHandler));
	}

	public HttpRequestGet(String url, ResponseHandler<T> responseHandler) {
		this(new Builder<T>().setUrl(url).setResponseParser(responseHandler));
	}

	protected HttpRequestGet(AbstractBuilder<T,?,?> builder) {
		super(builder);
	}
}
