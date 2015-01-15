package co.tophe.signed.oauth1;


import co.tophe.HttpEngine;
import co.tophe.signed.OAuthClientApp;
import co.tophe.signed.oauth1.internal.OAuth1RequestAdapter;

import oauth.signpost.AbstractOAuthConsumer;
import oauth.signpost.http.HttpRequest;

/**
 * Base class for an OAuth1 consumer app.
 * @see co.tophe.signed.oauth1.OAuth1ConsumerClocked
 */
public class HttpClientOAuth1Consumer extends AbstractOAuthConsumer {

	private static final long serialVersionUID = 8890615728426576510L;

	/**
	 * Constructor for the {@link co.tophe.signed.OAuthClientApp}
	 */
	public HttpClientOAuth1Consumer(OAuthClientApp clientApp) {
		super(clientApp.getConsumerKey(), clientApp.getConsumerSecret());
	}

	@Override
	protected HttpRequest wrap(Object request) {
		return new OAuth1RequestAdapter((HttpEngine<?,?>) request);
	}
}