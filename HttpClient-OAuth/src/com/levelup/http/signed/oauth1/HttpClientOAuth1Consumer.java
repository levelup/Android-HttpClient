package com.levelup.http.signed.oauth1;


import oauth.signpost.AbstractOAuthConsumer;
import oauth.signpost.http.HttpRequest;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.signed.OAuthClientApp;

public class HttpClientOAuth1Consumer extends AbstractOAuthConsumer {

	private static final long serialVersionUID = 8890615728426576510L;

	public HttpClientOAuth1Consumer(OAuthClientApp clientApp) {
		super(clientApp.getConsumerKey(), clientApp.getConsumerSecret());
	}

	@Override
	protected HttpRequest wrap(Object request) {
		return new OAuth1RequestAdapter((BaseHttpRequest<?>) request);
	}
}