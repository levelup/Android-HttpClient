package com.levelup.http.signed.oauth1;

import junit.framework.TestCase;
import oauth.signpost.exception.OAuthException;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpRequest;
import com.levelup.http.UriParams;
import com.levelup.http.signed.OAuthClientApp;
import com.levelup.http.signed.OAuthUser;

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

	protected static final HttpClientOAuth1Provider twitterAppProvider = new HttpClientOAuth1Provider(twitterApp, TWITTER_REQUEST_TOKEN, TWITTER_ACCESS_TOKEN, TWITTER_AUTHORIZE);

	public void testRequestToken() {
		try {
			assertNotNull(twitterAppProvider.retrieveRequestToken("androidhttp://request_token/"));
		} catch (OAuthException e) {
			fail(e.getMessage());
		}	
	}

	protected HttpRequest getSearchRequest() {
		RequestSignerOAuth1 twitterSigner = new RequestSignerOAuth1(twitterApp, twitterUser);
		UriParams searchParams = new UriParams(2);
		searchParams.add("q", "toto");
		searchParams.add("count", 5);
		return new BaseHttpRequest.Builder().setSigner(twitterSigner).setUrl("https://api.twitter.com/1.1/search/tweets.json", searchParams).build();
	}

	/**
	 * Do a <a href="https://dev.twitter.com/docs/api/1.1/get/search/tweets">twitter search query</a>
	 */
	public void testTwitterSearch() throws Exception {
		HttpRequest search = getSearchRequest();
		String response = HttpClient.getStringResponse(search);
		assertNotNull(response);
		assertTrue(response.length() > 0);
		assertEquals('{', response.charAt(0));
	}

	/**
	 * Do a <a href="https://dev.twitter.com/docs/api/1.1/get/friends/list">twitter friends list query</a>
	 */
	public void testFriendsList() throws Exception {
		RequestSignerOAuth1 twitterSigner = new RequestSignerOAuth1(twitterApp, twitterUser);
		UriParams uriParams = new UriParams(2);
		uriParams.add("cursor", -1);
		uriParams.add("screen_name", "twitterapi");
		HttpRequest request = new BaseHttpRequest.Builder().setSigner(twitterSigner).setUrl("https://api.twitter.com/1.1/friends/list.json", uriParams).build();
		String response = HttpClient.getStringResponse(request);
		assertNotNull(response);
		assertTrue(response.length() > 0);
		assertEquals('{', response.charAt(0));
	}

	/**
	 * Do a <a href="https://dev.twitter.com/docs/api/1.1/get/users/show">twitter user lookup</a>
	 */
	public void testUser() throws Exception {
		RequestSignerOAuth1 twitterSigner = new RequestSignerOAuth1(twitterApp, twitterUser);
		UriParams uriParams = new UriParams(1);
		uriParams.add("screen_name", "touiteurtest");
		HttpRequest request = new BaseHttpRequest.Builder().setSigner(twitterSigner).setUrl("https://api.twitter.com/1.1/users/show.json", uriParams).build();
		String response = HttpClient.getStringResponse(request);
		assertNotNull(response);
		assertTrue(response.length() > 0);
		assertEquals('{', response.charAt(0));
	}
}
