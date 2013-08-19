package com.levelup.http.signpost;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.SortedSet;

import oauth.signpost.OAuth;
import oauth.signpost.http.HttpParameters;
import android.text.TextUtils;

import com.levelup.http.HttpRequest;

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

	@Override
	public synchronized void sign(HttpRequest req, HttpURLConnection conn, HttpParameters oauthParams) {
		try {
			HttpURLConnection dummyConnection = (HttpURLConnection) new URL(verifyUrl).openConnection();
			HttpParameters realm = new HttpParameters();
			if (null!=oauthParams) {
				for (Entry<String, SortedSet<String>> entries : oauthParams.entrySet()) {
					realm.put(entries.getKey(), entries.getValue());
				}
			}
			if (!TextUtils.isEmpty(verifyRealm))
				realm.put("realm", verifyRealm);
			super.sign(req, dummyConnection, realm);

			String header = dummyConnection.getRequestProperty(OAuth.HTTP_AUTHORIZATION_HEADER);
			if (header!=null) {
				conn.setRequestProperty(OAuth.HTTP_AUTHORIZATION_HEADER, null);
				conn.setRequestProperty("X-Auth-Service-Provider", verifyUrl);
				conn.setRequestProperty("X-Verify-Credentials-Authorization", header);
			}
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
	}
}
