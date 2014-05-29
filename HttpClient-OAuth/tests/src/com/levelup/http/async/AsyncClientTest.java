package com.levelup.http.async;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;
import com.levelup.http.HttpRequestGet;
import com.levelup.http.InputStreamStringParser;

public class AsyncClientTest extends TestCase {

	private static final String BASIC_URL = "http://www.levelupstudio.com/";
	private static final String BASIC_URL_TAG = "test1";
	private static final String LARGE_URL = "http://video.webmfiles.org/big-buck-bunny_trailer.webm";
	private static final String BASIC_URL_HTTPS = "https://www.google.com/";
	private static final String LARGE_URL_HTTPS = "https://r5---sn-h5q7ener.googlevideo.com/videoplayback?upn=1FcIIG1R44M&ip=2.6.208.180&sparams=id%2Cip%2Cipbits%2Citag%2Cratebypass%2Crequiressl%2Csource%2Cupn%2Cexpire&requiressl=yes&sver=3&source=youtube&mv=m&ms=au&itag=18&ipbits=0&expire=1397658501&id=o-AB0c1o6tDQ6m9wnBycuEl-5fDEz0Pg20MeAX0W2f0Qvh&key=yt5&signature=1F574C12088A00E5B8A4FE0581C276079F52921C.E0C162202EEB022993F1874D001ED090BFEDD4E2&ratebypass=yes&fexp=900161%2C937417%2C913434%2C923328%2C936916%2C934022%2C936923&mt=1397634061&cpn=7R6rPhkfEpZ9usUK&ptk=AntenaTvGroupRomania&oid=9V7PWSIHHUBefBR9xKX70Q&pltype=contentugc&c=WEB&cver=html5";

	// TODO test with streaming connection (chunked over HTTPS with sometimes no data sent for 1 minute)
	// TODO test with streaming connection with SPDY
	// TODO test with long POST

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		HttpClient.setConnectionFactory(null); // make sure we don't use Okhttp
	}

	public void testAsyncSimpleQuery() {
		AsyncHttpClient.getString(BASIC_URL, BASIC_URL_TAG, null);
	}

	private static class TestAsyncCallback extends BaseNetworkCallback<String> {
		@Override
		public void onNetworkFailed(Throwable t) {
			if (t instanceof IOException) {
				// shit happens
			} else if (t instanceof HttpException && t.getCause() instanceof IOException) {
				// shit happens
			} else {
				fail(t.getMessage());
			}
		}
	}

	private static class TestLongAsyncCallback extends TestAsyncCallback {
		@Override
		public void onNetworkSuccess(String response) {
			fail("We're not supposed to have received this");
		}
	}

	@MediumTest
	public void testAsyncSimpleQueryResult() {
		final CountDownLatch latch = new CountDownLatch(1);

		AsyncHttpClient.getString(BASIC_URL, BASIC_URL_TAG, new TestAsyncCallback() {
			@Override
			public void onNetworkSuccess(String response) {
				latch.countDown();
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			fail("unreasonably slow");
		}
	}


	public void testCancelShort() {
		HttpRequest request = new HttpRequestGet(BASIC_URL);
		Future<String> downloadTask = AsyncHttpClient.doRequest(request, InputStreamStringParser.instance, new TestLongAsyncCallback());

		downloadTask.cancel(true);

		try {
			downloadTask.get();
		} catch(CancellationException e) {
			// fine
		} catch (InterruptedException e) {
			// fine
		} catch (ExecutionException e) {
			fail("the task did not exit correctly "+e);
		}
	}

	public void testCancelShortHttps() {
		HttpRequest request = new HttpRequestGet(BASIC_URL_HTTPS);
		Future<String> downloadTask = AsyncHttpClient.doRequest(request, InputStreamStringParser.instance, new TestLongAsyncCallback());

		downloadTask.cancel(true);

		try {
			downloadTask.get();
		} catch(CancellationException e) {
			// fine
		} catch (InterruptedException e) {
			// fine
		} catch (ExecutionException e) {
			fail("the task did not exit correctly "+e);
		}
	}

	public void testCancelLong() {
		HttpRequest request = new HttpRequestGet(LARGE_URL);
		Future<String> downloadTask = AsyncHttpClient.doRequest(request, InputStreamStringParser.instance, new TestLongAsyncCallback());
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}

		downloadTask.cancel(true);

		try {
			downloadTask.get();
		} catch(CancellationException e) {
			// fine
		} catch (InterruptedException e) {
			// fine
		} catch (ExecutionException e) {
			fail("the task did not exit correctly "+e);
		}
	}

	public void testCancelLongHttps() {
		HttpRequest request = new HttpRequestGet(LARGE_URL_HTTPS);
		Future<String> downloadTask = AsyncHttpClient.doRequest(request, InputStreamStringParser.instance, new TestLongAsyncCallback());
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}

		downloadTask.cancel(true);

		try {
			downloadTask.get();
		} catch(CancellationException e) {
			// fine
		} catch (InterruptedException e) {
			// fine
		} catch (ExecutionException e) {
			fail("the task did not exit correctly "+e);
		}
	}
}
