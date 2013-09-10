package com.levelup.http.signpost;

import junit.framework.TestCase;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpParamsGet;
import com.levelup.http.signpost.HttpRequestSignedGet;
import com.levelup.http.signpost.OAuthClientApp;
import com.levelup.http.signpost.OAuthUser;
import com.levelup.http.signpost.RequestSigner;

public abstract class AbstractTwitterTest extends TestCase {
	protected static final OAuthClientApp twitterApp = new OAuthClientApp() {
		@Override
		public String getConsumerSecret() {
			return "n7RCQdXIamonfiBqGayvi9QGzwZqIXtsXmO8ZTd8aCc";
		}
		
		@Override
		public String getConsumerKey() {
			return "STPlfE2JWMdgFw3Zwd8lw";
		}
	};
	
	protected static final OAuthUser twitterUser = new OAuthUser() {
		@Override
		public String getToken() {
			return "93009608-hIOBvpgiRFrFAEhHP1o3vm1s0EpqRslUX2EYSSYex";
		}
		@Override
		public String getTokenSecret() {
			return "ieI3JHQSjl4iwbC3eJKCHvV44Uo6WsJx2QPCsH8U";
		}
	};
	
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
