package com.levelup.http.signpost;

import android.net.Uri;

import com.levelup.http.HttpException;
import com.levelup.http.HttpUriParameters;
import com.levelup.http.HttpRequestGet;

public class HttpRequestSignedGet<T> extends HttpRequestGet<T> implements HttpRequestSigned {

	public static class Builder<T> extends HttpRequestGet.Builder<T> {

		private RequestSigner signer;

		public Builder<T> setSigner(RequestSigner signer) {
	        this.signer = signer;
	        return this;
        }

		public HttpRequestSignedGet<T> build() {
			return new HttpRequestSignedGet<T>(this);
		}
    }

	private final RequestSigner signer;

	protected HttpRequestSignedGet(Builder<T> builder) {
		super(builder);
		this.signer = builder.signer;
	}

	public HttpRequestSignedGet(RequestSigner signer, String baseUrl, HttpUriParameters httpParams) {
		super(baseUrl, httpParams, null);
		this.signer = signer;
	}

	public HttpRequestSignedGet(RequestSigner signer, Uri baseUri, HttpUriParameters httpParams) {
		super(baseUri, httpParams, null);
		this.signer = signer;
	}

	public HttpRequestSignedGet(RequestSigner signer, String url) {
		super(url, null);
		this.signer = signer;
	}
	
	@Override
	public void settleHttpHeaders() throws HttpException {
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
