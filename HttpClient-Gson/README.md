Android-HttpClient Gson module
===============================

A module for HttpClient that uses [gson][1] to parse the data received from HTTP.
Like the other `InputStreamParser` they can be used [../HttpClient](synchronously) or [../HttpClient-Async](asynchronously).

Sample Code
-----------

```java
class MyClass {
	@SerializedName("id") int id;
	@SerializedName("id") String name;
}

HttpRequestGet request = new HttpRequestGet("https://api.com/1/test.json");

// Create a parser for MyClass using Gson
Gson gson = new GsonBuilder().create();
InputStreamGsonParser<MyClass> parser = new InputStreamGsonParser<MyClass>(gson, MyClass.getType());

// query the data from the server directly in the form of an object of class MyClass
MyClass dlInstance = HttpClient.parseRequest(request, parser);
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

[1]: https://code.google.com/p/google-gson/
