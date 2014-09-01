package com.levelup.http;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import android.test.AndroidTestCase;

import com.levelup.http.parser.ResponseToString;
import com.levelup.http.parser.ResponseToVoid;

public class CallableHelperTest extends AndroidTestCase {

	public void testChain() throws Exception {
		BaseHttpRequest<String> mainRequest = new BaseHttpRequest.Builder<String>()
				.setUrl("http://httpbin.org/")
				.setResponseParser(ResponseToString.RESPONSE_HANDLER)
				.build();

		BaseHttpRequest<Void> resultIsFalseRequest = new BaseHttpRequest.Builder<Void>()
				.setUrl("http://httpbin.org/status/404")
				.setResponseParser(ResponseToVoid.RESPONSE_HANDLER)
				.build();

		HttpEngine<String> mainEngine = new HttpEngine.Builder<String>().setTypedRequest(mainRequest).build();
		final HttpEngine<Void> falseResultEngine = new HttpEngine.Builder<Void>().setTypedRequest(resultIsFalseRequest).build();

		CallableHelper.chainCallable(mainEngine, new CallableHelper.CallableForResult<String, Void>() {
			@Override
			public Callable<Void> getNextCallable(String s) throws HttpException {
				if (!"true".equals(s))
					return falseResultEngine;
				return null;
			}
		});
	}

	public void testPostData() throws Exception {
		BaseHttpRequest<String> mainRequest = new BaseHttpRequest.Builder<String>()
				.setUrl("http://httpbin.org/")
				.setResponseParser(ResponseToString.RESPONSE_HANDLER)
				.build();

		HttpEngine<String> mainEngine = new HttpEngine.Builder<String>().setTypedRequest(mainRequest).build();
		final Callable<String> trueResultCallable = new Callable<String>() {
			@Override
			public String call() throws Exception {
				return "this string indicates we received true";
			}
		};
		final Callable<String> falseResultCallable = new Callable<String>() {
			@Override
			public String call() throws Exception {
				return "something went wrong";
			}
		};

		CallableHelper.chainCallable(mainEngine, new CallableHelper.CallableForResult<String, String>() {
			@Override
			public Callable<String> getNextCallable(String s) throws HttpException {
				if ("true".equals(s))
					return trueResultCallable;
				else
					return falseResultCallable;
			}
		});
	}

	public void testDoubleChain() throws Exception {
		// TODO do the query 3 times using a chain of a chain
		BaseHttpRequest<String> mainRequest = new BaseHttpRequest.Builder<String>()
				.setUrl("http://httpbin.org/")
				.setResponseParser(ResponseToString.RESPONSE_HANDLER)
				.build();

		HttpEngine<String> mainEngine = new HttpEngine.Builder<String>().setTypedRequest(mainRequest).build();
	}

	private static HttpEngine<String> getLinkPageEngine(String link) {
		return new HttpEngine.Builder<String>()
				.setRequest(new RawHttpRequest.Builder()
						.setUrl(link)
						.build())
				.setResponseHandler(ResponseToString.RESPONSE_HANDLER)
				.build();
	}

	private static class NextLinkReader implements CallableHelper.CallableForResult<String,String> {
		private final ArrayList<String> links = new ArrayList<String>();

		@Override
		public Callable<String> getNextCallable(String s) throws HttpException {
			int linkIndex = s.indexOf("<a href='");
			while (-1 != linkIndex) {
				int linkEndIndex = s.indexOf("'>", linkIndex + 9);
				String link = "http://httpbin.org" + s.substring(linkIndex + 9, linkEndIndex);
				if (!links.contains(link)) {
					links.add(link);
					return CallableHelper.chainCallable(getLinkPageEngine(link), this);
				}
				linkIndex = s.indexOf("<a href='", linkEndIndex);
			}
			return null;
		}
	};

	public void testPagedData() throws Exception {
		// stop after the last page and send all the pages data in the end
		NextLinkReader pageReader = new NextLinkReader();

		HttpEngine<String> initialRequest = getLinkPageEngine("http://httpbin.org/links/3/0");
		Callable<String> chain = CallableHelper.chainCallable(initialRequest, pageReader);
		chain.call(); // always end with null

		assertEquals(3, pageReader.links.size());
		assertTrue(pageReader.links.contains("http://httpbin.org/links/3/0"));
		assertTrue(pageReader.links.contains("http://httpbin.org/links/3/1"));
		assertTrue(pageReader.links.contains("http://httpbin.org/links/3/2"));
	}
}