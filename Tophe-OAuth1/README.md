TOPHE OAuth1 module
===============================

A module for HttpClient that uses [oauth-signpost][1] to do OAuth HTTP signatures, including OAuth Echo signatures.

Sample Code
-----------

```java
OAuthClientApp appSignature = new OAuthClientApp() {
	public String getConsumerKey() {
		return "app-key";
	}
	public String getConsumerSecret() {
		return "app-secret";
	}
}
OAuthUser userSignature = new OAuthUser() {
	public String getToken() {
		return "user-token";
	}
	public String getTokenSecret() {
		return "user-token-secret";
	}
}
RequestSignerOAuth1 signer = new RequestSignerOAuth1(appSignature, userSignature);

UriParams uriParams = new UriParams(1);
uriParams.add("msg", "signed message");

HttpRequest get = new HttpRequestSignedGet.Builder().setUrl("http://my.com/hello", uriParams).setSigner(signer).build();

HttpClient.getQueryResponse(get);
```

<h2>Request a Twitter authentication key</h2>
```java
HttpClientOAuth1Provider provider = new HttpClientOAuth1Provider(appSignature,
    "https://twitter.com/oauth/request_token",
    "https://twitter.com/oauth/access_token",
    "https://twitter.com/oauth/authorize");

// The URL that will be launched in the WebView after the user authenticates successfully
String OAUTH_CALLBACK_URL = "twitteroauth://request_token/";

String webUrl = provider.retrieveRequestToken(OAUTH_CALLBACK_URL);

// launch the web browser for the user to authorize your app
webview.loadUrl(webUrl);

// track our OAUTH_CALLBACK_URL called by Twitter in the WebView
webview.setWebViewClient(new WebViewClient() {
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if (url.startsWith(OAUTH_CALLBACK_URL)) {
			Uri tokenUrl = Uri.parse(url);
			provider.retrieveAccessToken(tokenUrl.getQueryParameter("oauth_verifier"));
			
			String userToken = provider.getConsumer().getToken();
			String userSecret = provider.getConsumer().getTokenSecret();
			return true;
		}
		return false;
	};
});
```

License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[1]: https://code.google.com/p/oauth-signpost/
