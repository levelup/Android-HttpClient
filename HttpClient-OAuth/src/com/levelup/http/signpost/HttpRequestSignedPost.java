package com.levelup.http.signpost;

import android.net.Uri;

import com.levelup.http.HttpException;
import com.levelup.http.HttpBodyParameters;
import com.levelup.http.HttpRequestPost;

public class HttpRequestSignedPost<T> extends HttpRequestPost<T> implements HttpRequestSigned {

	public static class Builder<T> extends HttpRequestPost.Builder<T> {

		private RequestSigner signer;

		public Builder<T> setSigner(RequestSigner signer) {
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
		this.signer = builder.signer;
	}

	public HttpRequestSignedPost(RequestSigner signer, String url, HttpBodyParameters httpParams) {
		super(url, httpParams, null);
		this.signer = signer;
	}

	public HttpRequestSignedPost(RequestSigner signer, Uri uri, HttpBodyParameters httpParams) {
		super(uri, httpParams, null);
		this.signer = signer;
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
