package com.levelup.http.signed;

import android.text.TextUtils;

import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;

/**
 * Helper class to OAuth2 sign a {@link BaseHttpRequestSigned}
 */
public class RequestSignerOAuth2 extends AbstractRequestSigner {

	/**
	 * A {@link RequestSignerOAuth2} for the specified authenticating user
	 * @param user The use used to authenticate, may be {@code null}
	 */
	public RequestSignerOAuth2(OAuthUser user) {
		super(user);
	}

	@Override
	public void sign(HttpRequest req) throws HttpException {
		OAuthUser user = getOAuthUser();
		if (null != user) {
			String tokenSecret = user.getTokenSecret();
			if (!TextUtils.isEmpty(tokenSecret))
				req.setHeader("Authorization", "Bearer " + tokenSecret);
		}
	}
}
