Android-HttpClient OkHttp Module
================================

A module for HttpClient asynchronous client to easily do network queries outside of the UI thread.

Your `AsyncHttpCallback` is run in the UI thread after the query has finished processing the `InputStream`.
There are helper API to simply query a String. You can also use the HttpClient's `InputStreamParser` 
interface to proces the data the way you want in the network thread, for example to process the data with GSon.

By default the network tasks run in a pool of 3 * NumberOfProcessor but you can change the default Executor or use a different one per query.

Sample Code
-----------

```java
AsyncHttpClient.getString("http://www.levelupstudio.com/", null, new AsyncHttpCallback<String>() {
	@Override
	public void onHttpSuccess(String response) {
		// your String result here
	}

	@Override
	public void onHttpError(Throwable t) {
		// something went wrong
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