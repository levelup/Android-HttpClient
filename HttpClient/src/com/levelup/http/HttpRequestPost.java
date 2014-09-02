package com.levelup.http;

import android.net.Uri;

/**
 * Basic HTTP POST request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 * @see BaseHttpRequest for a more complete API
 */
public class HttpRequestPost<T> extends BaseHttpRequest<T> {
	public static abstract class AbstractBuilder<T, REQ extends HttpRequestPost<T>, BUILDER extends AbstractBuilder<T,REQ,BUILDER>> extends BaseHttpRequest.AbstractBuilder<T,REQ,BUILDER> {
		public AbstractBuilder() {
			setHttpMethod("POST");
		}
	}

	public static abstract class ChildBuilder<T, REQ extends HttpRequestPost<T>> extends AbstractBuilder<T, REQ, ChildBuilder<T, REQ>> {
	}

	public final static class Builder<T> extends AbstractBuilder<T, HttpRequestPost<T>, Builder<T>> {
		@Override
		protected HttpRequestPost<T> build(Builder<T> builder) {
			return new HttpRequestPost<T>(builder);
		}
	}

	public HttpRequestPost(String url, HttpBodyParameters bodyParams, ResponseHandler<T> responseHandler) {
		this(new Builder<T>().setBody(bodyParams).setUrl(url).setResponseHandler(responseHandler));
	}

	public HttpRequestPost(Uri uri, HttpBodyParameters bodyParams, ResponseHandler<T> responseHandler) {
		this(new Builder<T>().setBody(bodyParams).setUri(uri).setResponseHandler(responseHandler));
	}

	protected HttpRequestPost(AbstractBuilder<T,?,?> builder) {
		super(builder);
	}
}
