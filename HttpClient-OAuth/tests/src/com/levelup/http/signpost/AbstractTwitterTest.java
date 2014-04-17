package com.levelup.http.signpost;

import junit.framework.TestCase;
import oauth.signpost.exception.OAuthException;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpParamsGet;

public abstract class AbstractTwitterTest extends TestCase {
	protected static final OAuthClientApp twitterApp = new OAuthClientApp() {
		@Override
		public String getConsumerSecret() {
			return TwitterTokens.TWITTER_AUTH_SECRET;
		}
		
		@Override
		public String getConsumerKey() {
			return TwitterTokens.TWITTER_AUTH_KEY;
		}
	};
	
	protected static final OAuthUser twitterUser = new OAuthUser() {
		@Override
		public String getToken() {
			return TwitterTokens.TWITTER_USER_TOKEN;
		}
		@Override
		public String getTokenSecret() {
			return TwitterTokens.TWITTER_USER_SECRET;
		}
	};
	
	protected static final String TWITTER_REQUEST_TOKEN = "https://twitter.com/oauth/request_token";
	protected static final String TWITTER_ACCESS_TOKEN = "https://twitter.com/oauth/access_token";
	protected static final String TWITTER_AUTHORIZE = "https://twitter.com/oauth/authorize";

	protected static final HttpClientOAuthProvider twitterAppProvider = new HttpClientOAuthProvider(twitterApp, TWITTER_REQUEST_TOKEN, TWITTER_ACCESS_TOKEN, TWITTER_AUTHORIZE);
		
	public void testRequestToken() {
		try {
			assertNotNull(twitterAppProvider.retrieveRequestToken("androidhttp://request_token/"));
		} catch (OAuthException e) {
			fail(e.getMessage());
		}	
	}
	
	protected HttpRequestSignedGet getSearchRequest() {
		RequestSigner twitterSigner = new RequestSigner(twitterApp, twitterUser);
		HttpParamsGet searchParams = new HttpParamsGet(2);
		searchParams.add("q", "toto");
		searchParams.add("count", 5);
		return new HttpRequestSignedGet(twitterSigner, "https://api.twitter.com/1.1/search/tweets.json", searchParams);
	}
	
	/**
	 * Do a <a href="https://dev.twitter.com/docs/api/1.1/get/search/tweets">twitter search query</a>
	 */
	public void testTwitterSearch() throws Exception {
		HttpRequestSignedGet search = getSearchRequest();
		String response = HttpClient.getStringResponse(search);
		assertNotNull(response);
		assertTrue(response.length() > 0);
		assertEquals('{', response.charAt(0));
	}

	/**
	 * Do a <a href="https://dev.twitter.com/docs/api/1.1/get/friends/list">twitter friends list query</a>
	 */
	public void testFriendsList() throws Exception {
		RequestSigner twitterSigner = new RequestSigner(twitterApp, twitterUser);
		HttpParamsGet httpParams = new HttpParamsGet(2);
		httpParams.add("cursor", -1);
		httpParams.add("screen_name", "twitterapi");
		HttpRequestSignedGet request = new HttpRequestSignedGet(twitterSigner, "https://api.twitter.com/1.1/friends/list.json", httpParams);
		String response = HttpClient.getStringResponse(request);
		assertNotNull(response);
		assertTrue(response.length() > 0);
		assertEquals('{', response.charAt(0));
	}

	/**
	 * Do a <a href="https://dev.twitter.com/docs/api/1.1/get/users/show">twitter user lookup</a>
	 */
	public void testUser() throws Exception {
		RequestSigner twitterSigner = new RequestSigner(twitterApp, twitterUser);
		HttpParamsGet httpParams = new HttpParamsGet(1);
		httpParams.add("screen_name", "touiteurtest");
		HttpRequestSignedGet request = new HttpRequestSignedGet(twitterSigner, "https://api.twitter.com/1.1/users/show.json", httpParams);
		String response = HttpClient.getStringResponse(request);
		assertNotNull(response);
		assertTrue(response.length() > 0);
		assertEquals('{', response.charAt(0));
	}
}
