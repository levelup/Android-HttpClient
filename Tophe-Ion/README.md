#TOPHE Ion module

A module for TopheClient that uses [Ion][1] as the HTTP engine. You can also construct an `HttpEngineIon` yourself and use
the `requestBuilder` field to do additional work on the Ion request, like using the query to load a picture.

##Sample Code

###Setup the Ion engine

Make Tophe use [Ion][1] by default for all HTTP requests. The Ion engine doesn't support `HttpStream` output so the default
engine will be used in that case.

```java
IonClient.setup(context);
```

###Get the Ion request to retrieve a Bitmap

```java
BaseHttpRequest<InputStream, ServerException> request = new BaseHttpRequest.Builder<InputStream, ServerException>()
        .setUrl("https://ton.twitter.com/dmpics/somepic51215423.jpg")
        .setResponseHandler(new BaseResponseHandler<InputStream>(XferTransformResponseInputStream.INSTANCE))
        .setErrorParser(BodyToServerException.INSTANCE)
        .build();

IonHttpEngineFactory.getInstance(context);
HttpEngineIon<InputStream, ServerException> ionRequest = (HttpEngineIon<InputStream, ServerException>)
        new HttpEngine.Builder<InputStream, ServerException>()
        .setTypedRequest(request)
        .build();

ionRequest.requestBuilder.intoImageView(imageView);
```

## Download

Download [the latest JAR][2] or grab via Maven [![Maven Central](https://maven-badges.herokuapp.com/maven-central/co.tophe/tophe-ion/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/co.tophe/tophe-ion)
```xml
<dependency>
  <groupId>co.tophe</groupId>
  <artifactId>tophe-ion</artifactId>
  <version>1.0.0</version>
</dependency>
```
or Gradle:
```groovy
compile 'co.tophe:tophe-ion:1.0.0'
```

## License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[1]: https://github.com/koush/ion
[2]: https://search.maven.org/remote_content?g=co.tophe&a=tophe-ion&v=LATEST

