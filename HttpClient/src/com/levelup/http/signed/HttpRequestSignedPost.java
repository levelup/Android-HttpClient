package com.levelup.http.signed;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpRequestImpl;
import com.levelup.http.HttpRequestPost;


/**
 * @deprecated use {@link BaseHttpRequest.Builder#setSigner(com.levelup.http.RequestSigner) BaseHttpRequest.Builder.setSigner()} 
 */
@Deprecated
public class HttpRequestSignedPost<T> extends HttpRequestPost<T> {

	public static class Builder<T> extends HttpRequestPost.Builder<T> {
		@Override
		public HttpRequestSignedPost<T> build() {
			return (HttpRequestSignedPost<T>) super.build();
		}

		@Override
		public BaseHttpRequest<T> build(HttpRequestImpl<T> impl) {
			return new HttpRequestSignedPost(impl);
		}
	}

	/**
	 * @deprecated use {@link BaseHttpRequest} 
	 */
	protected HttpRequestSignedPost(Builder<T> builder) {
		super(builder);
	}

	/**
	 * @deprecated use {@link BaseHttpRequest}
	 */
	protected HttpRequestSignedPost(HttpRequestImpl<T> impl) {
		super(impl);
	}
}
