package com.levelup.http.signed;

import com.levelup.http.HttpException;
import com.levelup.http.HttpRequestGet;

public class HttpRequestSignedGet<T> extends HttpRequestGet<T> implements HttpRequestSigned {

	public static class Builder<T> extends HttpRequestGet.Builder<T> {

		private AbstractRequestSigner signer;

		public Builder<T> setSigner(AbstractRequestSigner signer) {
			if (null==signer) {
				throw new IllegalArgumentException();
			}
	        this.signer = signer;
	        return this;
        }

		public HttpRequestSignedGet<T> build() {
			return new HttpRequestSignedGet<T>(this);
		}
    }

	private final AbstractRequestSigner signer;

	protected HttpRequestSignedGet(Builder<T> builder) {
		super(builder);
		if (builder.signer==null) {
			throw new NullPointerException();
		}
		this.signer = builder.signer;
	}

	@Override
	public void settleHttpHeaders() throws HttpException {
		super.settleHttpHeaders();
		signer.sign(this);
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
