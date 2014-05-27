package com.levelup.http.signpost;

import oauth.signpost.AbstractOAuthConsumer;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.http.HttpParameters;
import android.text.TextUtils;

import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;

/**
 * Helper class to OAuth2 sign a {@link HttpRequestSigned}
 */
public class RequestSignerOAuth2 extends RequestSigner {

	private static final OAuthConsumer DUMMY_APP = new AbstractOAuthConsumer(null, null) {
		private static final long serialVersionUID = 5083754143285228980L;
		@Override
		protected oauth.signpost.http.HttpRequest wrap(Object arg0) {
			throw new IllegalStateException();
		}
	};

	/**
	 * A {@link RequestSignerOAuth2} for the specified authenticating user
	 * @param user The use used to authenticate, may be {@code null}
	 */
	public RequestSignerOAuth2(OAuthUser user) {
		super(DUMMY_APP, user);
	}

	@Override
	public void sign(HttpRequest req, HttpParameters oauthParams) throws HttpException {
		OAuthUser user = getOAuthUser();
		if (null != user) {
			String tokenSecret = user.getTokenSecret();
			if (!TextUtils.isEmpty(tokenSecret))
				req.setHeader("Authorization", "Bearer " + tokenSecret);
		}
	}
}
