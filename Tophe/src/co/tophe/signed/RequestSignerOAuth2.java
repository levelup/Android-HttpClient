package co.tophe.signed;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import co.tophe.HttpEngine;

/**
 * Helper class to sign a {@link co.tophe.BaseHttpRequest} with OAuth2.
 */
public class RequestSignerOAuth2 extends AbstractOAuthSigner {

	/**
	 * A {@link RequestSignerOAuth2} for the specified authenticating user.
	 *
	 * @param user The use used to authenticate, may be {@code null}.
	 */
	public RequestSignerOAuth2(@Nullable OAuthUser user) {
		super(user);
	}

	@Override
	public void sign(HttpEngine<?, ?> req) {
		OAuthUser user = getOAuthUser();
		if (null != user) {
			String tokenSecret = user.getTokenSecret();
			if (!TextUtils.isEmpty(tokenSecret))
				req.setHeader("Authorization", "Bearer " + tokenSecret);
		}
	}
}
