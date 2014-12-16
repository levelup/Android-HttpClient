package co.tophe.signed.oauth1;

import java.util.Map.Entry;
import java.util.SortedSet;

import android.text.TextUtils;

import co.tophe.BaseHttpRequest;
import co.tophe.HttpAuthException;
import co.tophe.HttpEngine;
import co.tophe.HttpRequest;
import co.tophe.ServerException;
import co.tophe.parser.BodyToString;
import co.tophe.signed.OAuthClientApp;
import co.tophe.signed.OAuthUser;

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
	public void sign(HttpEngine<?,?> req, HttpParameters oauthParams) throws HttpAuthException {
		HttpParameters realm = new HttpParameters();
		if (null!=oauthParams) {
			for (Entry<String, SortedSet<String>> entries : oauthParams.entrySet()) {
				realm.put(entries.getKey(), entries.getValue());
			}
		}
		if (!TextUtils.isEmpty(verifyRealm))
			realm.put("realm", verifyRealm);

	    BaseHttpRequest<String,ServerException> echoReq = new BaseHttpRequest.Builder<String,ServerException>()
			    .setUrl(verifyUrl)
			    .setResponseHandler(BodyToString.RESPONSE_HANDLER)
			    .build();
	    HttpEngine<String,ServerException> engine = new HttpEngine.Builder<String,ServerException>().setTypedRequest(echoReq).build();
		super.sign(engine, realm);

		String header = engine.getHeader(OAuth.HTTP_AUTHORIZATION_HEADER);
	    if (null==header) {
		    throw (HttpAuthException) new HttpAuthException.Builder(req.getHttpRequest(), req.getHttpResponse())
				    .setErrorMessage("request not properly signed")
				    .build();
	    }

		req.setHeader("X-Verify-Credentials-Authorization", header);
		req.setHeader("X-Auth-Service-Provider", verifyUrl);
	}
}
