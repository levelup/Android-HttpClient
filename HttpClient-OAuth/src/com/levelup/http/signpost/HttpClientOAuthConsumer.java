package com.levelup.http.signpost;


import oauth.signpost.AbstractOAuthConsumer;

import com.levelup.http.HttpRequest;

public class HttpClientOAuthConsumer extends AbstractOAuthConsumer {

	private static final long serialVersionUID = 8890615728426576510L;

	public HttpClientOAuthConsumer(OAuthClientApp clientApp) {
		super(clientApp.getConsumerKey(), clientApp.getConsumerSecret());
	}

	@Override
	protected oauth.signpost.http.HttpRequest wrap(Object request) {
		return new OAuthRequestAdapter((HttpRequest) request);
	}
}