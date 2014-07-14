package com.levelup.http.signed;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpRequestImpl;
import com.levelup.http.HttpRequestPost;


/**
 * @deprecated use {@link BaseHttpRequest.Builder#setSigner(com.levelup.http.RequestSigner) BaseHttpRequest.Builder.setSigner()} 
 */
@Deprecated
public class HttpRequestSignedPost<T> extends HttpRequestPost<T> {

	public static abstract class AbstractBuilder<T, R extends HttpRequestSignedPost<T>> extends HttpRequestPost.AbstractBuilder<T, R> {
	}

	public final static class Builder<T> extends AbstractBuilder<T,HttpRequestSignedPost<T>> {
		@Override
		public final HttpRequestSignedPost<T> build(HttpRequestImpl<T> impl) {
			return new HttpRequestSignedPost(impl);
		}
	}

	/**
	 * @deprecated use {@link BaseHttpRequest}
	 */
	protected HttpRequestSignedPost(HttpRequestImpl<T> impl) {
		super(impl);
	}
}
