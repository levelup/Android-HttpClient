package com.levelup.http.signpost;

import java.util.Map.Entry;
import java.util.SortedSet;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.http.HttpParameters;
import android.text.TextUtils;

import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;
import com.levelup.http.HttpRequestGet;

/**
 * Helper class to Echo OAuth sign a {@link HttpRequest} using <a href="https://code.google.com/p/oauth-signpost/">oauth-signpost</a>
 */
public class RequestSignerEcho extends RequestSigner {

	private final String verifyUrl;
	private final String verifyRealm;

	public RequestSignerEcho(OAuthClientApp clientApp, OAuthUser user, String verifyUrl, String verifyRealm) {
		super(clientApp, user);
		this.verifyUrl = verifyUrl;
		this.verifyRealm = verifyRealm;
	}

	public RequestSignerEcho(OAuthConsumer clientApp, OAuthUser user, String verifyUrl, String verifyRealm) {
		super(clientApp, user);
		this.verifyUrl = verifyUrl;
		this.verifyRealm = verifyRealm;
	}

	@Override
	public void sign(HttpRequest req, HttpParameters oauthParams) throws HttpException {
		HttpRequest echoReq = new HttpRequestGet(verifyUrl);
		HttpParameters realm = new HttpParameters();
		if (null!=oauthParams) {
			for (Entry<String, SortedSet<String>> entries : oauthParams.entrySet()) {
				realm.put(entries.getKey(), entries.getValue());
			}
		}
		if (!TextUtils.isEmpty(verifyRealm))
			realm.put("realm", verifyRealm);
		super.sign(echoReq, realm);

		String header = echoReq.getHeader(OAuth.HTTP_AUTHORIZATION_HEADER);
		if (header!=null) {
			req.setHeader("X-Verify-Credentials-Authorization", header);
			req.setHeader("X-Auth-Service-Provider", verifyUrl);
		}
	}
}
