package com.levelup.http.signed.oauth1;


import com.levelup.http.HttpEngine;
import com.levelup.http.signed.OAuthClientApp;
import com.levelup.http.signed.oauth1.internal.OAuth1RequestAdapter;

import oauth.signpost.AbstractOAuthConsumer;
import oauth.signpost.http.HttpRequest;

public class HttpClientOAuth1Consumer extends AbstractOAuthConsumer {

	private static final long serialVersionUID = 8890615728426576510L;

	public HttpClientOAuth1Consumer(OAuthClientApp clientApp) {
		super(clientApp.getConsumerKey(), clientApp.getConsumerSecret());
	}

	@Override
	protected HttpRequest wrap(Object request) {
		return new OAuth1RequestAdapter((HttpEngine<?>) request);
	}
}