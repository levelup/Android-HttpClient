package com.levelup.http.signpost;

import com.levelup.http.HttpBodyParameters;
import com.levelup.http.HttpException;
import com.levelup.http.HttpRequestPost;

public class HttpRequestSignedPost<T> extends HttpRequestPost<T> implements HttpRequestSigned {

	public static class Builder<T> extends HttpRequestPost.Builder<T> {

		private RequestSigner signer;

		public Builder(HttpBodyParameters bodyParams) {
			super(bodyParams);
		}

		public Builder<T> setSigner(RequestSigner signer) {
			if (null==signer) {
				throw new IllegalArgumentException();
			}
			this.signer = signer;
			return this;
		}

		public HttpRequestSignedPost<T> build() {
			return new HttpRequestSignedPost<T>(this);
		}
	}

	private final RequestSigner signer;

	protected HttpRequestSignedPost(Builder<T> builder) {
		super(builder);
		if (builder.signer==null) {
			throw new NullPointerException();
		}
		this.signer = builder.signer;
	}

	@Override
	public void settleHttpHeaders() throws HttpException  {
		super.settleHttpHeaders();
		signer.sign(this, null);
	}

	@Override
	public HttpException.Builder newException() {
		return new HttpExceptionSigned.Builder(this);
	}

	@Override
	public OAuthUser getOAuthUser() {
		if (null == signer)
			return null;
		return signer.getOAuthUser();
	}

	@Override
	protected String getToStringExtra() {
		String result = super.getToStringExtra();
		return result + " for " + getOAuthUser();
	}
}
