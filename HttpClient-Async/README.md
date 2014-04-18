Android-HttpClient Asynchronous Module
================================

A module for HttpClient asynchronous client to easily do network queries outside of the UI thread.

Your `AsyncHttpCallback` is run in the UI thread after the query has finished processing the `InputStream`.
There are helper API to simply query a String. You can also use the HttpClient's `InputStreamParser` 
interface to proces the data the way you want in the network thread, for example to process the data with GSon.

By default the network tasks run in a pool of 3 * NumberOfProcessor but you can change the default Executor or use a different one per query.

Sample Code
-----------

```java
AsyncHttpClient.getString("http://www.levelupstudio.com/", null, new BaseAsyncHttpCallback<String>() {
	@Override
	public void onHttpSuccess(String response) {
		// your String result here
	}
});
```

<h2>Sample to cancel a download</h2>
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

<h2>Sample with JSONObject reader</h2>
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

<h2>Sample with a custom InputStreamParser</h2>
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
