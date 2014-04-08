package com.levelup.http.async;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;
import com.levelup.http.HttpRequestGet;
import com.levelup.http.InputStreamStringParser;

public class AsyncClientTest extends TestCase {

	private static final String BASIC_URL = "http://www.levelupstudio.com/";
	private static final String BASIC_URL_TAG = "test1";
	private static final String LARGE_URL = "http://video.webmfiles.org/big-buck-bunny_trailer.webm";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		HttpClient.setConnectionFactory(null); // make sure we don't use Okhttp
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
				} else if (t instanceof HttpException && t.getCause() instanceof IOException) {
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


	public void testCancelShort() {
		HttpRequest request = new HttpRequestGet(BASIC_URL);
		Future<String> downloadTask = AsyncHttpClient.doRequest(request, InputStreamStringParser.instance, new AsyncHttpCallback<String>() {
			@Override
			public void onHttpSuccess(String response) {
				fail("We're not supposed to have received this");
			}

			@Override
			public void onHttpError(Throwable t) {
				if (t instanceof IOException) {
					// shit happens
				} else if (t instanceof HttpException && t.getCause() instanceof IOException) {
					// shit happens
				} else {
					fail(t.getMessage());
				}
			}
		});
		downloadTask.cancel(true);
	}

	public void testCancelLong() {
		HttpRequest request = new HttpRequestGet(LARGE_URL);
		Future<String> downloadTask = AsyncHttpClient.doRequest(request, InputStreamStringParser.instance, new AsyncHttpCallback<String>() {
			@Override
			public void onHttpSuccess(String response) {
				fail("We're not supposed to have received this");
			}

			@Override
			public void onHttpError(Throwable t) {
				if (t instanceof IOException) {
					// shit happens
				} else if (t instanceof HttpException && t.getCause() instanceof IOException) {
					// shit happens
				} else {
					fail(t.getMessage());
				}
			}
		});
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		downloadTask.cancel(true);
	}
}
