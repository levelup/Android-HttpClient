package com.levelup.http.signed;

import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;

public abstract class AbstractRequestSigner {

	private final OAuthUser user;

	public AbstractRequestSigner(OAuthUser user) {
		this.user = user;
	}
	
	public OAuthUser getOAuthUser() {
		return user;
	}
	
	protected abstract void sign(HttpRequest req) throws HttpException;
}
