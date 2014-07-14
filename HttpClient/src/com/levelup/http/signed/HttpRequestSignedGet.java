package com.levelup.http.signed;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpRequestGet;
import com.levelup.http.HttpRequestImpl;


/**
 * @deprecated use {@link BaseHttpRequest.Builder#setSigner(com.levelup.http.RequestSigner) BaseHttpRequest.Builder.setSigner()} 
 */
@Deprecated
public class HttpRequestSignedGet<T> extends HttpRequestGet<T> {

	public static abstract class AbstractBuilder<T,R extends HttpRequestSignedGet<T>> extends HttpRequestGet.AbstractBuilder<T,R> {
	}

	public final static class Builder<T> extends AbstractBuilder<T,HttpRequestSignedGet<T>> {
		@Override
		public final HttpRequestSignedGet<T> build(HttpRequestImpl<T> impl) {
			return new HttpRequestSignedGet(impl);
		}
	}

	/**
	 * @deprecated use {@link BaseHttpRequest}
	 */
	protected HttpRequestSignedGet(HttpRequestImpl<T> impl) {
		super(impl);
	}
}
