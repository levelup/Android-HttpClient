package com.levelup.http;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import android.content.Context;
import android.test.AndroidTestCase;

import com.levelup.http.paging.NextPageFactory;
import com.levelup.http.paging.PageCallback;
import com.levelup.http.paging.PagingHelper;
import com.levelup.http.parser.BodyToString;
import com.levelup.http.parser.BodyTransformChain;
import com.levelup.http.parser.Transformer;

public class CallableHelperTest extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		HttpClient.setup(context);
	}

	private static class PagedResult {
		private final ArrayList<String> links = new ArrayList<String>();
	}

	private static class Page {
		private final ArrayList<String> pageLinks = new ArrayList<String>();
	}

	private static final ResponseHandler<Page> PAGE_RESPONSE_HANDLER = new ResponseHandler<Page>(
			BodyTransformChain.Builder
					// read the data as a String
					.init(BodyToString.INSTANCE)
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

	private static HttpEngine<Page> getPageEngine(String link) {
		return new HttpEngine.Builder<Page>()
				.setRequest(new RawHttpRequest.Builder().setUrl(link).build())
				.setResponseHandler(PAGE_RESPONSE_HANDLER)
				.build();
	}

	public void testPagedData() throws Exception {
		final PagedResult initData = new PagedResult();

		Callable<PagedResult> mainCallable = PagingHelper.processPage(getPageEngine("http://httpbin.org/links/3/0")
				, initData
				, new PageCallback<PagedResult, Page>() {
					@Override
					public void onNewPage(PagedResult pagesHolder, Page page) {
						for (String link : page.pageLinks) {
							if (!pagesHolder.links.contains(link)) {
								// we found a link we haven't read yet (a page), read it
								pagesHolder.links.add(link);
								break;
							}
						}
					}
				},
				new NextPageFactory<Page>() {
					@Override
					public Callable<Page> getNextCallable(Page page) {
						for (String link : page.pageLinks) {
							if (!initData.links.contains(link)) {
								// we found a link we haven't read yet (a page), read it
								return getPageEngine(link);
							}
						}
						return null;
					}
				}
		);

		PagedResult result = mainCallable.call();

		assertEquals(3, result.links.size());
		assertTrue(result.links.contains("http://httpbin.org/links/3/0"));
		assertTrue(result.links.contains("http://httpbin.org/links/3/1"));
		assertTrue(result.links.contains("http://httpbin.org/links/3/2"));
	}

}