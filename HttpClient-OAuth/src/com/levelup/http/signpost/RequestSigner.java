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

	/**
	 * A {@link RequestSigner} for the specified clientApp and user authenticating
	 * @param clientApp The {@link OAuthClientApp} used to sign the HTTP queries 
	 * @param user The use used to authenticate, may be {@code null}
	 */
	public RequestSigner(OAuthClientApp clientApp, OAuthUser user) {
		if (null == clientApp) throw new NullPointerException("We need an OAuthClientApp to authenticate");
		//if (null == user) throw new NullPointerException("We need a OAuthUser to authenticate");
		this.mOAuthConsumer = new HttpClientOAuthConsumer(clientApp);
		this.user = user;
	}

	/**
	 * A {@link RequestSigner} for the specified consumer ({@link OAuthConsumer signpost class}) and user authenticating
	 * @param consumer The {@link OAuthConsumer} used to sign if you don't want to use a {@link OAuthClientApp}
	 * @param user The use used to authenticate, may be {@code null}
	 */
	public RequestSigner(OAuthConsumer consumer, OAuthUser user) {
		if (null == consumer) throw new NullPointerException("We need an OAuthConsumer to authenticate");
		//if (null == user) throw new NullPointerException("We need a OAuthUser to authenticate");
		this.mOAuthConsumer = consumer;
		this.user = user;
	}

	public OAuthUser getOAuthUser() {
		return user;
	}

	public void sign(HttpRequest req, HttpParameters oauthParams) throws HttpException {
		synchronized (mOAuthConsumer) {
			if (null!=user) {
				mOAuthConsumer.setTokenWithSecret(user.getToken(), user.getTokenSecret());
			} else {
				mOAuthConsumer.setTokenWithSecret("", "");
			}
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
