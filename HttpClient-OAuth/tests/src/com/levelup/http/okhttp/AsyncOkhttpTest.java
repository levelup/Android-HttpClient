package com.levelup.http.okhttp;

import com.levelup.http.HttpClient;
import com.levelup.http.async.AsyncClientTest;

public class AsyncOkhttpTest extends AsyncClientTest {

	protected void setUp() throws Exception {
		super.setUp();
		HttpClient.setConnectionFactory(OkHttpClient.instance);
	}

}
