package com.levelup.http.signed;

import com.levelup.http.RequestSigner;


public abstract class AbstractRequestSigner implements RequestSigner {

	private final OAuthUser user;

	protected AbstractRequestSigner(OAuthUser user) {
		this.user = user;
	}
	
	public OAuthUser getOAuthUser() {
		return user;
	}
	
}
