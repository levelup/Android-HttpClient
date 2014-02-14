Android-HttpClient OkHttp Module
================================

A module for HttpClient that uses [Volley][1] for data transfer to make use of its asynchronous system.

Sample Code
-----------

```java
import com.levelup.http.HttpRequestGet;
import com.levelup.http.volley.HttpRequestVolley;

RequestQueue req;

HttpRequestGet get = new HttpRequestGet("http://levelupstudio.com/");
VolleyHttpRequest volleyRequest = new VolleyHttpRequest(get, null, null);

req.add(volleyRequest);
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

[1]: https://android.googlesource.com/platform/frameworks/volley/
