package com.levelup.http.signpost;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.http.HttpParameters;

import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;

/**
 * Helper class to OAuth sign a {@link HttpRequest} using <a href="https://code.google.com/p/oauth-signpost/">oauth-signpost</a>
 */
public class RequestSigner {

	private final OAuthUser user;
	private OAuthConsumer mOAuthConsumer;

	public RequestSigner(OAuthClientApp clientApp, OAuthUser user) {
		if (null == clientApp) throw new NullPointerException("We need an OAuthClientApp to authenticate");
		if (null == user) throw new NullPointerException("We need a OAuthUser to authenticate");
		this.mOAuthConsumer = new HttpClientOAuthConsumer(clientApp);
		this.user = user;
	}

	public RequestSigner(OAuthConsumer consumer, OAuthUser user) {
		if (null == consumer) throw new NullPointerException("We need an OAuthConsumer to authenticate");
		if (null == user) throw new NullPointerException("We need a OAuthUser to authenticate");
		this.mOAuthConsumer = consumer;
		this.user = user;
	}

	public OAuthUser getOAuthUser() {
		return user;
	}

	public void sign(HttpRequest req, HttpParameters oauthParams) {
		synchronized (mOAuthConsumer) {
			mOAuthConsumer.setTokenWithSecret(user.getToken(), user.getTokenSecret());
			mOAuthConsumer.setAdditionalParameters(oauthParams);

			try {
				mOAuthConsumer.sign(req);
			} catch (OAuthException e) {
				HttpException.Builder builder = req.newException();
				builder.setErrorCode(HttpException.ERROR_AUTH);
				builder.setErrorMessage("Bad OAuth for "+user+" on "+req);
				builder.setCause(e);
				throw builder.build();
			}
		}
	}
}
