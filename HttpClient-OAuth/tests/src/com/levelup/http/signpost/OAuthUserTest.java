package com.levelup.http.signpost;

import junit.framework.TestCase;

import com.levelup.http.HttpClient;
import com.levelup.http.UriParams;
import com.levelup.http.HttpRequest;

public class OAuthUserTest extends TestCase {

	private static final OAuthClientApp testApp = new OAuthClientApp() {
		@Override
		public String getConsumerSecret() {
			return "secret";
		}

		@Override
		public String getConsumerKey() {
			return "consumer-key";
		}
	};
	
	public void testNullUser() throws Exception {
		RequestSigner signer = new RequestSigner(testApp, null);
		UriParams httpParams = new UriParams(1);
		httpParams.add("msg", "signed message");

		HttpRequest get = new HttpRequestSignedGet(signer, "http://www.levelupstudio.com/", httpParams);

		HttpClient.getQueryResponse(get);
	}

	public void testEmptyUser() throws Exception {
		OAuthUser emptyUser = new OAuthUser() {
			@Override
			public String getTokenSecret() {
				return null;
			}

			@Override
			public String getToken() {
				return null;
			}
		};

		RequestSigner signer = new RequestSigner(testApp, emptyUser);
		UriParams httpParams = new UriParams(1);
		httpParams.add("msg", "signed message");

		HttpRequest get = new HttpRequestSignedGet(signer, "http://www.levelupstudio.com/", httpParams);

		HttpClient.getQueryResponse(get);
	}

}
