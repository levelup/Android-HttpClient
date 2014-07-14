package com.levelup.http.signed;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpEngine;
import com.levelup.http.HttpRequestGet;


/**
 * @deprecated use {@link BaseHttpRequest.Builder#setSigner(com.levelup.http.RequestSigner) BaseHttpRequest.Builder.setSigner()} 
 */
@Deprecated
public class HttpRequestSignedGet<T> extends HttpRequestGet<T> {

	public static abstract class AbstractBuilder<T,R extends HttpRequestSignedGet<T>> extends HttpRequestGet.AbstractBuilder<T,R> {
	}

	public final static class Builder<T> extends AbstractBuilder<T,HttpRequestSignedGet<T>> {
		@Override
		public final HttpRequestSignedGet<T> build(HttpEngine<T> impl) {
			return new HttpRequestSignedGet(impl);
		}
	}

	/**
	 * @deprecated use {@link BaseHttpRequest}
	 */
	protected HttpRequestSignedGet(HttpEngine<T> impl) {
		super(impl);
	}
}
