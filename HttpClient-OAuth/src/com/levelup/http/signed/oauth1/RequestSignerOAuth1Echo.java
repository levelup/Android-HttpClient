package com.levelup.http.signed.oauth1;

import java.util.Map.Entry;
import java.util.SortedSet;

import android.text.TextUtils;

import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;
import com.levelup.http.HttpRequestGet;
import com.levelup.http.signed.OAuthClientApp;
import com.levelup.http.signed.OAuthUser;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.http.HttpParameters;

/**
 * Helper class to Echo OAuth sign a {@link HttpRequest} using <a href="https://code.google.com/p/oauth-signpost/">oauth-signpost</a>
 */
public class RequestSignerOAuth1Echo extends RequestSignerOAuth1 {

	private final String verifyUrl;
	private final String verifyRealm;

	public RequestSignerOAuth1Echo(OAuthClientApp clientApp, OAuthUser user, String verifyUrl, String verifyRealm) {
		super(clientApp, user);
		this.verifyUrl = verifyUrl;
		this.verifyRealm = verifyRealm;
	}

	public RequestSignerOAuth1Echo(OAuthConsumer clientApp, OAuthUser user, String verifyUrl, String verifyRealm) {
		super(clientApp, user);
		this.verifyUrl = verifyUrl;
		this.verifyRealm = verifyRealm;
	}

    @Override
	public void sign(HttpRequest req, HttpParameters oauthParams) throws HttpException {
		HttpParameters realm = new HttpParameters();
		if (null!=oauthParams) {
			for (Entry<String, SortedSet<String>> entries : oauthParams.entrySet()) {
				realm.put(entries.getKey(), entries.getValue());
			}
		}
		if (!TextUtils.isEmpty(verifyRealm))
			realm.put("realm", verifyRealm);
		HttpRequestGet<Void> echoReq = new HttpRequestGet<Void>(verifyUrl);
		super.sign(echoReq, realm);

		String header = echoReq.getHeader(OAuth.HTTP_AUTHORIZATION_HEADER);
		if (header!=null) {
			req.setHeader("X-Verify-Credentials-Authorization", header);
			req.setHeader("X-Auth-Service-Provider", verifyUrl);
		}
	}
}
