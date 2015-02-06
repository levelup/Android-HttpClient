#TOPHE - Typed Object Processing HTTP Engine

An Android library to make HTTP calls with parameters easier.

There is a module to support OAuth1 signatures using [oauth-signpost][1] and a module to use [Ion][3] as the HTTP engine (by default it uses java's `HttpUrlConnection`).

##Features

* Strongly typed output data
	* support [Gson][4] parsing out of the box
	* possibility to chain parsing to get the final type
	* `ServerException` is an exception thrown when the server returns an error, it can be customized to parse the server data further (than a JSON object or `InputStream`)
	* always needs a parser, default ones are provided
* `Builder` based queries to minimize code size
* Multipart POST of File and InputStream
* URL-encoded JSON data
* URL-encoded String data
* easy addition of parameters to GET queries
* lightweight on memory
* single `TopheException` thrown from the `TopheClient`, with subclasses for finer exception handling
* support for timeouts per queries
* custom logging per HTTP query
* support for high-level cookie handling
* set the user language for all HTTP queries
* Security
	* disables SSLv3 by default
	* uses Google's conscrypt SSL stack from the Play Services when available

#Design

Each query is based on a `BaseHttpRequest` and is created through `BaseHttpRequest.Builder`. `HttpRequestGet` and `HttpRequestPost` a simplified versions that can be constructed without a Builder.

Each query requires 2 types of parser:

* the Content Parser that will turn a `HttpResponse` into your output/parsed object. 
* the Error Parser that will turn a`HttpResponse` into the parsed data of a `ServerException`

You can combine these 2 parsers into a `ResponseHandler`. The parsers should be thread-safe/reentrant.

A parser is a `XferTransform` that takes an `HttpResponse` object coming from the HTTP engine and turn it into a parsed object like a `String`, a `JSONObject`, an `HttpStream` (a continuous/live stream) or more elaborate types parsed with [Gson][4]. You can chain transforms to get the desired output type using `XferTransformChain`.

By convention the transforms that transform the `HttpResponse` body (be it a regular response or error data) are called Body transforms. There are a lot of predefined transforms in the library:

* BodyViaGson: parse the data using a [`gson`][4] object.
* BodyToJSONObject
* BodyToJSONArray
* BodyToHttpStream: an `HttpStream` is a "live/continuous" representation of the HTTP response body.
* BodyToServerException: the basic body parser to handle server error data.
* BodyToString
* BodyToVoid


#Sample Code

##Synchronous client

Upload a file using multipart encoding and get the response as a String.
```java
TopheClient.setup(context); // done once in your application

HttpBodyMultiPart multipart = new HttpBodyMultiPart();
multipart.addFile("file", myImageFile, "image/png");
multipart.add("text", "#selfie");

HttpRequest post = new BaseHttpRequest.Builder()
	.setUrl("http://my.com/picture.upload")
	.setBody(multipart)
	.build();

try {
   String response = TopheClient.getStringResponse(post);
} catch (TopheException e) {
}
```

###Sample with a ResponseHandler

Using [Gson][4] to parse the response. There is also a `BodyViaGson.asList()` method to easily get a `List` of the specified object type.

```java
BaseResponseHandler<ApiObject> responseHandler = new BaseResponseHandler<ApiObject>(
        new BodyViaGson<ApiObject>(ApiObject.class)
);

TypedHttpRequest<ApiObject, ServerException> apiGet = new BaseHttpRequest.Builder<ApiObject, ServerException>()
        .setUrl("http://my.com/api.json")
        .setResponseHandler(responseHandler)
        .build();

try {
    ApiObject parsed = TopheClient.parseRequest(apiGet);
} catch (ServerException e) {
} catch (HttpException e) {
}
```

##Asynchronous client

Your `AsyncCallback` is run in the UI thread after the query has finished processing the response body.
There are helper API to simply query a String. You can also use the TopheClient's `ResponseHandler` 
interface to process the data the way you want in the network thread.

By default the network tasks run in a pool of 3 * NumberOfProcessor but you can change the default Executor or set a different one per query.

Internally the asynchronous client turns a `BaseHttpRequest` into an `AsyncTask`. You can build your own async task using
`AsyncTask.Builder`.

```java
TypedHttpRequest<Spanned, ServerException> getHtml= new BaseHttpRequest.Builder<Spanned, ServerException>()
        .setUrl("http://www.levelupstudio.com/")
        .setContentParser(BodyTransformChain.createBuilder(BodyToString.INSTANCE)
                .addDataTransform(new Transformer<String, Spanned>() {
                    @Override
                    protected Spanned transform(String s) {
                        return Html.fromHtml(s);
                    }
                })
                .build())
        .setErrorParser(BodyToServerException.INSTANCE)
        .build();

AsyncTopheClient.postRequest(getHtml, new BaseAsyncCallback<Spanned>() {
    @Override
    public void onAsyncResult(Spanned result) {
        // your parsed HTML result here
    }
});
```

###Sample to cancel a download

```java
HttpRequestGet<String> request = new HttpRequestGet<String>("http://www.levelupstudio.com/", null, BodyToString.INSTANCE);
Future<String> downloadTask = AsyncTopheClient.postRequest(request, new BaseAsyncCallback<String>() {
    @Override
    public void onAsyncResult(String result) {
        // the HTML data as a String
    }
});

// cancel the download we just started/queued
downloadTask.cancel(true);
```

###Sample with JSONObject reader

```java
// Do the JSON API query in the background and get the result in the UI thread
TypedHttpRequest<JSONObject, ServerException> request = new HttpRequestGet<JSONObject>("http://service.com/api.json", null, BodyToJSONObject.INSTANCE);
AsyncTopheClient.postRequest(request, new BaseAsyncCallback<JSONObject>() {
    @Override
    public void onAsyncResult(JSONObject response) {
        // the object parsed from JSON data, called in the UI thread
    }
});
```


##OAuth signature

The TopheClient also provides a simple API for signing HTTP requests. A `RequestSigner` for OAuth2 is provided by default.
There is also a module for OAuth1 signature using [oauth-signpost][1].

```java
OAuthUser facebookUser = new OAuthUser() {
    public String getToken() {
        return "user-token";
    }

    public String getTokenSecret() {
        return "user-token-secret";
    }
};
RequestSigner facebookUserSigner = new RequestSignerOAuth2(facebookUser);

TypedHttpRequest<JSONObject, ServerException> signedFacebook = new BaseHttpRequest.Builder<JSONObject, ServerException>()
        .setUrl("http://graph.facebook.com/me")
        .setSigner(facebookUserSigner)
        .setResponseHandler(BodyToJSONObject.RESPONSE_HANDLER)
        .build();

try {
    JSONObject facebookProfileData = TopheClient.parseRequest(signedFacebook);
} catch (ServerException e) {
} catch (HttpException e) {
}
```

## Download

Download [the latest JAR][2] or grab via Maven [![Maven Central](https://maven-badges.herokuapp.com/maven-central/co.tophe/tophe/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/co.tophe/tophe)
```xml
<dependency>
  <groupId>co.tophe</groupId>
  <artifactId>tophe</artifactId>
  <version>1.0.0</version>
</dependency>
```
or Gradle:
```groovy
compile 'co.tophe:tophe:1.0.0'
compile 'co.tophe:tophe-oauth1:1.0.0'
compile 'co.tophe:tophe-ion:1.0.0'
```

##License

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
[2]: https://search.maven.org/remote_content?g=co.tophe&a=tophe&v=LATEST
[3]: https://github.com/koush/ion
[4]: https://code.google.com/p/google-gson/
