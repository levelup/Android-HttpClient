package com.levelup.http.signed;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpRequestPost;


/**
 * @deprecated use {@link BaseHttpRequest.Builder#setSigner(com.levelup.http.RequestSigner) BaseHttpRequest.Builder.setSigner()} 
 */
@Deprecated
public class HttpRequestSignedPost<T> extends HttpRequestPost<T> {

	public static class Builder<T> extends HttpRequestPost.Builder<T> {
		public HttpRequestSignedPost<T> build() {
			return new HttpRequestSignedPost<T>(this);
		}
	}

	/**
	 * @deprecated use {@link BaseHttpRequest} 
	 */
	protected HttpRequestSignedPost(Builder<T> builder) {
		super(builder);
	}
}
