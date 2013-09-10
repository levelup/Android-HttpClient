package com.levelup.http.signpost;

import com.levelup.http.HttpClient;

public class TwitterTest extends AbstractTwitterTest {
	protected void setUp() throws Exception {
		super.setUp();
		HttpClient.setConnectionFactory(null);
	};
}
