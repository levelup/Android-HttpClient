package com.levelup.http.signed.oauth1;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.http.HttpParameters;

import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;
import com.levelup.http.signed.AbstractRequestSigner;
import com.levelup.http.signed.OAuthClientApp;
import com.levelup.http.signed.OAuthUser;

/**
 * Helper class to OAuth sign a {@link HttpRequestSigned} using <a href="https://code.google.com/p/oauth-signpost/">oauth-signpost</a>
 */
public class RequestSignerOAuth1 extends AbstractRequestSigner {

	private final OAuthConsumer mOAuthConsumer;

	/**
	 * A {@link RequestSignerOAuth1} for the specified clientApp and user authenticating
	 * @param clientApp The {@link OAuthClientApp} used to sign the HTTP queries 
	 * @param user The use used to authenticate, may be {@code null}
	 */
	public RequestSignerOAuth1(OAuthClientApp clientApp, OAuthUser user) {
		super(user);
		if (null == clientApp) throw new NullPointerException("We need an OAuthClientApp to authenticate");
		//if (null == user) throw new NullPointerException("We need a OAuthUser to authenticate");
		this.mOAuthConsumer = new HttpClientOAuth1Consumer(clientApp);
	}

	/**
	 * A {@link RequestSignerOAuth1} for the specified consumer ({@link OAuthConsumer signpost class}) and user authenticating
	 * @param consumer The {@link OAuthConsumer} used to sign if you don't want to use a {@link OAuthClientApp}
	 * @param user The use used to authenticate, may be {@code null}
	 */
	public RequestSignerOAuth1(OAuthConsumer consumer, OAuthUser user) {
		super(user);
		if (null == consumer) throw new NullPointerException("We need an OAuthConsumer to authenticate");
		//if (null == user) throw new NullPointerException("We need a OAuthUser to authenticate");
		this.mOAuthConsumer = consumer;
	}

	@Override
	public void sign(HttpRequest req) throws HttpException {
		sign(req, null);
	}
	
	public void sign(HttpRequest req, HttpParameters oauthParams) throws HttpException {
		synchronized (mOAuthConsumer) {
			if (null!=getOAuthUser()) {
				mOAuthConsumer.setTokenWithSecret(getOAuthUser().getToken(), getOAuthUser().getTokenSecret());
			} else {
				mOAuthConsumer.setTokenWithSecret("", "");
			}
			mOAuthConsumer.setAdditionalParameters(oauthParams);

			try {
				mOAuthConsumer.sign(req);
			} catch (OAuthException e) {
				HttpException.Builder builder = req.newException();
				builder.setErrorCode(HttpException.ERROR_AUTH);
				builder.setErrorMessage("Bad OAuth for "+getOAuthUser()+" on "+req);
				builder.setCause(e);
				throw builder.build();
			}
		}
	}
}
