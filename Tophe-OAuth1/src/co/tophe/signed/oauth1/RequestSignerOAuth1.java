package co.tophe.signed.oauth1;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.tophe.HttpSignException;
import co.tophe.HttpEngine;
import co.tophe.signed.AbstractRequestSigner;
import co.tophe.signed.OAuthClientApp;
import co.tophe.signed.OAuthUser;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.http.HttpParameters;

/**
 * Helper class to add a {@link co.tophe.BaseHttpRequest BaseHttpRequest} OAuth1 signature
 * using <a href="https://code.google.com/p/oauth-signpost/">oauth-signpost</a>
 */
public class RequestSignerOAuth1 extends AbstractRequestSigner {

	private final OAuthConsumer mOAuthConsumer;

	/**
	 * A {@link RequestSignerOAuth1} for the specified clientApp and user authenticating
	 * @param clientApp The {@link OAuthClientApp} used to sign the HTTP queries 
	 * @param user The use used to authenticate, may be {@code null}
	 */
	public RequestSignerOAuth1(@NonNull OAuthClientApp clientApp, @Nullable OAuthUser user) {
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
	public RequestSignerOAuth1(@NonNull OAuthConsumer consumer, @Nullable OAuthUser user) {
		super(user);
		if (null == consumer) throw new NullPointerException("We need an OAuthConsumer to authenticate");
		//if (null == user) throw new NullPointerException("We need a OAuthUser to authenticate");
		this.mOAuthConsumer = consumer;
	}

	@Override
	public void sign(HttpEngine<?,?> req) throws HttpSignException {
		sign(req, null);
	}
	
	public void sign(HttpEngine<?,?> req, HttpParameters oauthParams) throws HttpSignException {
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
				HttpSignException.Builder builder = new HttpSignException.Builder(req.getHttpRequest());
				builder.setErrorMessage("Bad OAuth for "+getOAuthUser()+" on "+req);
				builder.setCause(e);
				throw builder.build();
			}
		}
	}
}
