package co.tophe.paging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.test.AndroidTestCase;

import co.tophe.BaseResponseHandler;
import co.tophe.TopheClient;
import co.tophe.HttpEngine;
import co.tophe.RawHttpRequest;
import co.tophe.ServerException;
import co.tophe.async.BaseAsyncCallback;
import co.tophe.parser.BodyToString;
import co.tophe.parser.BodyTransformChain;
import co.tophe.parser.Transformer;

public class PagingHelperTest extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		TopheClient.setup(context);
	}

	private static class PagedResult {
		private final ArrayList<String> links = new ArrayList<String>();
	}

	private static class Page {
		private final ArrayList<String> pageLinks = new ArrayList<String>();
	}

	private static final BaseResponseHandler<Page> PAGE_RESPONSE_HANDLER = new BaseResponseHandler<Page>(
			BodyTransformChain
					// read the data as a String
					.createBuilder(BodyToString.INSTANCE)
					// parse the String data to retrieve the links
					.addDataTransform(new Transformer<String, Page>() {
						@Override
						protected Page transform(String s) {
							Page result = new Page();
							int linkIndex = s.indexOf("<a href='");
							while (-1 != linkIndex) {
								int linkEndIndex = s.indexOf("'>", linkIndex + 9);
								String link = "http://httpbin.org" + s.substring(linkIndex + 9, linkEndIndex);
								result.pageLinks.add(link);
								linkIndex = s.indexOf("<a href='", linkEndIndex);
							}
							return result;
						}
					}).build()
	);

	private static HttpEngine<Page,ServerException> createPageEngine(String link) {
		return new HttpEngine.Builder<Page,ServerException>()
				.setRequest(new RawHttpRequest.Builder().setUrl(link).build())
				.setResponseHandler(PAGE_RESPONSE_HANDLER)
				.build();
	}


	public static void testListPageReader() throws Exception {
		Callable<List<Page>> pagesReader = PagingHelper.readPages(
				createPageEngine("http://httpbin.org/links/3/0"),
				new NextPageFactory<Page>() {
					@Override
					public Callable<Page> createCallable(Page page) {
						if ("http://httpbin.org/links/3/2".equals(page.pageLinks.get(1))) {
							if ("http://httpbin.org/links/3/1".equals(page.pageLinks.get(0))) {
								return createPageEngine("http://httpbin.org/links/3/1");
							} else {
								return createPageEngine("http://httpbin.org/links/3/2");
							}
						}
						return null;
					}
				}
		);

		List<Page> result = pagesReader.call();

		assertEquals(3, result.size());
		assertTrue(result.get(1).pageLinks.contains("http://httpbin.org/links/3/0"));
		assertTrue(result.get(2).pageLinks.contains("http://httpbin.org/links/3/1"));
		assertTrue(result.get(0).pageLinks.contains("http://httpbin.org/links/3/2"));
	}

	public void testListPagesAsync() throws Exception {
		HttpEngine<Page,ServerException> initialRequest = createPageEngine("http://httpbin.org/links/3/0");

		final CountDownLatch latch = new CountDownLatch(1);
		PagingHelper.readPagesAsync(initialRequest,
				new NextPageFactory<Page>() {
					@Override
					public Callable<Page> createCallable(Page page) {
						if ("http://httpbin.org/links/3/2".equals(page.pageLinks.get(1))) {
							if ("http://httpbin.org/links/3/1".equals(page.pageLinks.get(0))) {
								return createPageEngine("http://httpbin.org/links/3/1");
							} else {
								return createPageEngine("http://httpbin.org/links/3/2");
							}
						}
						return null;
					}
				},
				new BaseAsyncCallback<List<Page>>() {
					@Override
					public void onAsyncResult(List<Page> result) {
						super.onAsyncResult(result);

						assertEquals(3, result.size());
						assertTrue(result.get(1).pageLinks.contains("http://httpbin.org/links/3/0"));
						assertTrue(result.get(2).pageLinks.contains("http://httpbin.org/links/3/1"));
						assertTrue(result.get(0).pageLinks.contains("http://httpbin.org/links/3/2"));

						latch.countDown();
					}

					@Override
					public void onAsyncFailed(Throwable t) {
						super.onAsyncFailed(t);

						fail("failed on a request "+t);
					}

				});
		latch.await(40, TimeUnit.SECONDS);
	}
}