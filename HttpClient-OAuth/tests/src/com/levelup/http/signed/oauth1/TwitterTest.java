package com.levelup.http.signed.oauth1;

import com.levelup.http.HttpClient;

public class TwitterTest extends AbstractTwitterTest {
	protected void setUp() throws Exception {
		super.setUp();
		HttpClient.setConnectionFactory(null);
	}
}
