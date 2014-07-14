package com.levelup.http.signed;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpRequestGet;
import com.levelup.http.HttpRequestImpl;


/**
 * @deprecated use {@link BaseHttpRequest.Builder#setSigner(com.levelup.http.RequestSigner) BaseHttpRequest.Builder.setSigner()} 
 */
@Deprecated
public class HttpRequestSignedGet<T> extends HttpRequestGet<T> {

	public static class Builder<T> extends HttpRequestGet.Builder<T> {
		@Override
		public HttpRequestSignedGet<T> build() {
			return (HttpRequestSignedGet<T>) super.build();
		}

		@Override
		public HttpRequestGet<T> build(HttpRequestImpl<T> impl) {
			return new HttpRequestSignedGet(impl);
		}
	}

	/**
	 * @deprecated use {@link BaseHttpRequest} 
	 */
	protected HttpRequestSignedGet(Builder<T> builder) {
		super(builder);
	}

	/**
	 * @deprecated use {@link BaseHttpRequest}
	 */
	protected HttpRequestSignedGet(HttpRequestImpl<T> impl) {
		super(impl);
	}
}
