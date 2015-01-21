package co.tophe.signed.oauth1;


import java.security.SecureRandom;

import org.apache.commons.codec.binary.Hex;

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

	private final SecureRandom random = new SecureRandom();

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

	@Override
	protected String generateNonce() {
		byte[] generated = new byte[8];
		random.nextBytes(generated);
		return new String(Hex.encodeHex(generated));
	}
}