package com.levelup.http.signed;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpRequestGet;


/**
 * @deprecated use {@link BaseHttpRequest.Builder#setSigner(com.levelup.http.RequestSigner) BaseHttpRequest.Builder.setSigner()} 
 */
@Deprecated
public class HttpRequestSignedGet<T> extends HttpRequestGet<T> {

	public static class Builder<T> extends HttpRequestGet.Builder<T> {
		public HttpRequestSignedGet<T> build() {
			return new HttpRequestSignedGet<T>(this);
		}
	}

	/**
	 * @deprecated use {@link BaseHttpRequest} 
	 */
	protected HttpRequestSignedGet(Builder<T> builder) {
		super(builder);
	}
}
