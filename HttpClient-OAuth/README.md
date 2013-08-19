Android-HttpClient OAuth module
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
RequestSigner signer = new RequestSigner(appSignature, userSignature);

HttpParamsGet httpParams = new HttpParamsGet(1);
httpParams.add("msg", "signed message");

HttpRequestSignedGet get = new HttpRequestSignedGet(signer, "http://my.com/hello", httpParams);

String response = HttpClient.getQueryResponse(get);
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
