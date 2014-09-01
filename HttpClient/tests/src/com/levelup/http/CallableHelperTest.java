package com.levelup.http;

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

		CallableHelper.chain(mainEngine, new CallableHelper.CallableForResult<String, Void>() {
			@Override
			public Callable<Void> getResultCallable(String s) throws HttpException {
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

		CallableHelper.chain(mainEngine, new CallableHelper.CallableForResult<String, String>() {
			@Override
			public Callable<String> getResultCallable(String s) throws HttpException {
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

	public void testPagedData() throws Exception {
		// TODO stop after the last page and send all the pages data in the end
	}
}