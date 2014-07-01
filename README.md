Android-HttpClient
==================

An Android library to make HTTP calls with parameters easier.

There is a module to support OAuth1 signatures using [oauth-signpost][1].

There is a module that supports [OkHttp][2] for SPDY and other enhancements.

Features
--------

* Multipart POST of File and InputStream
* URL-encoded JSON data
* URL-encoded String data
* easy addition of parameters to GET queries
* lightweight on memory
* get the result of a query directly as a String
* single Exception type thrown from the `HttpClient`
* possibility to process the InputStream to parse it without coding a whole client
* support for timeouts per queries
* custom logging per HTTP query
* support for high-level cookie handling
* set the user language for all HTTP queries

Sample Code
-----------

<h2>Synchronous client</h2>
```java
HttpBodyMultiPart multipart = new HttpBodyMultiPart();
multipart.addFile("file", myImageFile, "image/png");
multipart.add("text", "#selfie");

HttpRequest post = new BaseHttpRequest.Builder()
	.setUrl("http://my.com/picture.upload")
	.setBody(multipart)
	.build();

String response = HttpClient.getStringResponse(post);
```

<h3>Sample with an InputStreamParser</h3>
```java
InputStreamParser<ApiObject> parser = new InputStreamParser<ApiObject>() {
	@Override
	public ApiObject parseInputStream(InputStream inputStream, HttpRequest request) throws IOException {
		// Process your InputStream
		JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
		try {
			return readMessagesArray(reader);
		} finally {
			reader.close();
		}
	}
}
TypedHttpRequest<ApiObject> apiGet = new BaseHttpRequest.Builder()
	.setUrl("http://my.com/api.json")
	.setStreamParser(parser)
	.build();

ApiObject parsed = HttpClient.parseRequest(apiGet);
```

<h2>Asynchronous client</h2>
Your `NetworkCallback` is run in the UI thread after the query has finished processing the response body.
There are helper API to simply query a String. You can also use the HttpClient's `InputStreamParser` 
interface to proces the data the way you want in the network thread, for example to process the data with GSon.

By default the network tasks run in a pool of 3 * NumberOfProcessor but you can change the default Executor or use a different one per query.

```java
AsyncHttpClient.getString("http://www.levelupstudio.com/", null, new BaseAsyncHttpCallback<String>() {
	@Override
	public void onHttpSuccess(String response) {
		// your String result here
	}
});
```

<h3>Sample to cancel a download</h3>
```java
HttpRequest request = new HttpRequestGet("http://www.levelupstudio.com/");
Future<String> downloadTask = AsyncHttpClient.doRequest(request, InputStreamStringParser.instance, new BaseAsyncHttpCallback<String>() {
	@Override
	public void onHttpSuccess(String response) {
		// the HTML code of the web page
	}
});

// cancel the download we just started/queued
downloadTask.cancel(true);
```

<h3>Sample with JSONObject reader</h3>
```java
// Do the JSON API query in the background and get the result in the UI thread
HttpRequest request = new HttpRequestGet("http://service.com/api.json");
AsyncHttpClient.doRequest(request, InputStreamJSONObjectParser.instance, new BaseAsyncHttpCallback<MyObject>() {
	@Override
	public void onHttpSuccess(JSONObject response) {
		// the object parsed from JSON data, called in the UI thread
	}
});
```

<h3>Sample with a custom InputStreamParser</h3>
```java
// Generic parser to turn some JSON data into your own MyObject class
final static InputStreamParser<MyObject> JsonToObject = new InputStreamParser<MyObject>) {
	@Override
	public MyObject parseInputStream(InputStream inputStream, HttpRequest request) throws IOException {
		// Process your InputStream
		JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
		try {
			return jsonReaderToMyObject(reader);
		} finally {
			reader.close();
		}
	}
}

// Do the JSON API query in the background and get the result in the UI thread
HttpRequest request = new HttpRequestGet("http://service.com/api.json");
AsyncHttpClient.doRequest(request, JsonToObject, new BaseAsyncHttpCallback<MyObject>() {
	@Override
	public void onHttpSuccess(MyObject response) {
		// the object parsed from JSON data, called in the UI thread
	}
});
```

<h2>Sample with Gson reader</h2>
A module for HttpClient that uses [gson][1] to parse the data received from HTTP.
Like the other `InputStreamParser` they can be used [synchronously or asynchronously](../HttpClient).

Sample Code
-----------

```java
class MyClass {
	@SerializedName("id") int id;
	@SerializedName("name") String name;
}

HttpRequestGet request = new HttpRequestGet("https://api.com/1/test.json");

// Create a parser for MyClass using Gson
Gson gson = new GsonBuilder().create();
InputStreamGsonParser<MyClass> parser = new InputStreamGsonParser<MyClass>(gson, MyClass.class);

// query the data from the server directly in the form of an object of class MyClass
MyClass dlInstance = HttpClient.parseRequest(request, parser);
```


<h2>OAuth signature</h2>
The HTTPClient also provides a simple API for signing HTTP requests. A `RequestSigner` for OAuth2 is provided
by default. There is also a module for OAuth1 signature using [oauth-signpost][1].

```java
OAuthUser facebookUser = new OAuthUser() {
	public String getToken() {
		return "user-token";
	}
	public String getTokenSecret() {
		return "user-token-secret";
	}
}
RequestSigner facebookUserSigner = new RequestSignerOAuth2(facebookUser);

HttpRequest signedFacebook = new BaseHttpRequest.Builder()
	.setUrl("http://graph.facebook.com/me")
	.setRequestSigner(facebookUserSigner)
	.build();

JSONObject fbData = HttpClient.parseRequest(signedFacebook, InputStreamJSONObjectParser.instance);
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
[2]: https://github.com/square/okhttp
