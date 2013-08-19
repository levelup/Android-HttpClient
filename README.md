Android-HttpClient
==================

An Android library to make HTTP calls with parameters easier.

There is a module to support OAuth signatures using oauth-signpost.
There is a module that supports OkHttp for SPDY and other enhancements.

Features
--------

* Multipart POST of File and InputStream
* URL-encoded form data
* URL-encoded JSON data
* easy addition of parameters to GET queries
* get the result of a query directly in a String
* single Exception type returned from the `HttpClient`
* set the user language for all HTTP queries
* support for timeouts per queries
* custom logging per HTTP query
* support for high-level cookie handling

Sample Code
-----------

```java
HttpParamsMultiPart multipart = new HttpParamsMultiPart();
multipart.add(myImageFile,"image/png");
multipart.add("text","my picture");

HttpRequestPost post = new HttpRequestPost("http://my.com/picture.upload", multipart);

String response = HttpClient.getStringResponse(post);
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

