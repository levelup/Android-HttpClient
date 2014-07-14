package com.levelup.http;

import android.net.Uri;

/**
 * Basic HTTP POST request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 * @see BaseHttpRequest for a more complete API
 */
public class HttpRequestPost<T> extends BaseHttpRequest<T> {
	public static abstract class AbstractBuilder<T, R extends HttpRequestPost<T>> extends BaseHttpRequest.AbstractBuilder<T,R> {
		public AbstractBuilder() {
			setHttpMethod("POST");
		}
	}

	public final static class Builder<T> extends AbstractBuilder<T,HttpRequestPost<T>> {
		@Override
		protected final HttpRequestPost<T> build(HttpEngine<T> impl) {
			return new HttpRequestPost<T>(impl);
		}
	}

	public HttpRequestPost(String url, HttpBodyParameters bodyParams) {
		this(new Builder<T>().setBody(bodyParams).setUrl(url).buildImpl());
	}

	public HttpRequestPost(Uri uri, HttpBodyParameters bodyParams) {
		this(new Builder<T>().setBody(bodyParams).setUri(uri).buildImpl());
	}

	protected HttpRequestPost(HttpEngine<T> impl) {
		super(impl);
	}
}
