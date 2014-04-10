package com.levelup.http.async;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
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
	private static final String BASIC_URL_HTTPS = "https://www.google.com/";
	private static final String LARGE_URL_HTTPS = "https://r9---sn-h5q7dn7z.googlevideo.com/videoplayback?source=youtube&upn=Dnfh7W6F4SE&mv=m&sparams=id%2Cip%2Cipbits%2Citag%2Cratebypass%2Crequiressl%2Csource%2Cupn%2Cexpire&signature=D3957D5C696B6C837DAC7CDCB4AA8B9063535409.C9922A68DB258307C6A8473870829E5012089A0F&key=yt5&ip=90.55.187.57&requiressl=yes&ms=au&fexp=902904%2C919122%2C939939%2C945031%2C916807%2C936207%2C936108%2C940204%2C937417%2C913434%2C936916%2C934022%2C936921%2C936923&mt=1397033661&ratebypass=yes&expire=1397056090&itag=18&ipbits=0&id=o-AMplDYPpfxK9OzIL_ooaTp101GKK7z4syFDckhfeY_pN&sver=3&cpn=Iv1jJdzergLmN6Wz&ptk=youtube_none&pltype=contentugc";

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

	public void testAsyncSimpleQueryResult() {
		final CountDownLatch latch = new CountDownLatch(1);

		AsyncHttpClient.getString(BASIC_URL, BASIC_URL_TAG, new TestAsyncCallback() {
			@Override
			public void onNetworkSuccess(String response) {
				latch.countDown();
			}
		});
		try {
			latch.await(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			fail("unreanably slow");
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
