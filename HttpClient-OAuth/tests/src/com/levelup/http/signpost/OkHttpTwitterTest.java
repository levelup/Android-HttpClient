package com.levelup.http.signpost;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpParamsGet;
import com.levelup.http.okhttp.OkHttpClient;

public class OkHttpTwitterTest extends AbstractTwitterTest {
	protected void setUp() throws Exception {
		HttpClient.setConnectionFactory(OkHttpClient.instance);
	};
	
	public void testIdentityEncoding() throws Exception {
		RequestSigner twitterSigner = new RequestSigner(twitterApp, twitterUser);
		HttpParamsGet searchParams = new HttpParamsGet(2);
		searchParams.add("q", "toto");
		searchParams.add("count", 5);
		HttpRequestSignedGet search = new HttpRequestSignedGet(twitterSigner, "https://api.twitter.com/1.1/search/tweets.json", searchParams);
		search.addHeader("Accept-Encoding", "identity");
		String response = HttpClient.getStringResponse(search);
		assertNotNull(response);
		assertTrue(response.length() > 0);
		assertEquals('{', response.charAt(0));
	}

	public void testGzipEncoding() throws Exception {
		RequestSigner twitterSigner = new RequestSigner(twitterApp, twitterUser);
		HttpParamsGet searchParams = new HttpParamsGet(2);
		searchParams.add("q", "toto");
		searchParams.add("count", 5);
		HttpRequestSignedGet search = new HttpRequestSignedGet(twitterSigner, "https://api.twitter.com/1.1/search/tweets.json", searchParams);
		search.addHeader("Accept-Encoding", "gzip");
		String response = HttpClient.getStringResponse(search);
		assertNotNull(response);
		assertTrue(response.length() > 0);
		assertEquals('{', response.charAt(0));
	}

}
