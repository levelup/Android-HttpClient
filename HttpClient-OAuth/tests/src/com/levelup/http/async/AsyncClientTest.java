package com.levelup.http.async;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

public class AsyncClientTest extends TestCase {

	private static final String BASIC_URL = "http://www.levelupstudio.com/";
	private static final String BASIC_URL_TAG = "test1";
	
	public AsyncClientTest() {
	}
	
	public void testAsyncSimpleQuery() {
		AsyncHttpClient.getString(BASIC_URL, BASIC_URL_TAG, null);
	}
	
	public void testAsyncSimpleQueryResult() {
		final CountDownLatch latch = new CountDownLatch(1);
		
		AsyncHttpClient.getString(BASIC_URL, BASIC_URL_TAG, new AsyncHttpCallback<String>() {
			@Override
			public void onHttpSuccess(String response) {
				latch.countDown();
			}
			
			@Override
			public void onHttpError(Throwable t) {
				if (t instanceof IOException) {
					// shit happens
				} else {
					fail(t.getMessage());
				}
				
			}
		});
		try {
			latch.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
	}
	
	pul
	
	// TODO: test canceling a long download to see if the cancel() has an effect
}
