package co.tophe.signed.oauth1;

import android.content.Context;
import android.test.AndroidTestCase;

import co.tophe.BaseHttpRequest;
import co.tophe.TopheClient;
import co.tophe.UriParams;
import co.tophe.parser.BodyToString;
import co.tophe.signed.OAuthClientApp;
import co.tophe.signed.OAuthUser;

public class OAuthUserTest extends AndroidTestCase {

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

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		TopheClient.setup(context);
	}
	
	public void testNullUser() throws Exception {
		RequestSignerOAuth1 signer = new RequestSignerOAuth1(testApp, null);
		UriParams uriParams = new UriParams(1);
		uriParams.add("msg", "signed message");

		BaseHttpRequest<String> get = new BaseHttpRequest.Builder<String>().
				setSigner(signer).
				setUrl("http://www.levelupstudio.com/", uriParams).
				setResponseHandler(BodyToString.RESPONSE_HANDLER).
				build();
		TopheClient.parseRequest(get);
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

		RequestSignerOAuth1 signer = new RequestSignerOAuth1(testApp, emptyUser);
		UriParams uriParams = new UriParams(1);
		uriParams.add("msg", "signed message");

		BaseHttpRequest<String> get = new BaseHttpRequest.Builder<String>().
				setSigner(signer).
				setUrl("http://www.levelupstudio.com/", uriParams).
				setResponseHandler(BodyToString.RESPONSE_HANDLER).
				build();
		TopheClient.parseRequest(get);
	}

}
