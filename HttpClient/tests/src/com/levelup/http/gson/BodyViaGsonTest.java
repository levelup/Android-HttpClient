package com.levelup.http.gson;

import android.content.Context;
import android.test.AndroidTestCase;

import com.google.gson.annotations.SerializedName;
import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpDataParserException;
import com.levelup.http.HttpFailureException;
import com.levelup.http.ResponseHandler;
import com.levelup.http.parser.BodyToString;
import com.levelup.http.parser.HttpFailureHandlerViaXferTransform;

public class BodyViaGsonTest extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		HttpClient.setup(context);
	}

	private static class HttpbinData {
		@SerializedName("url") String url;
	}

	public void testGsonData() throws Exception {
		BaseHttpRequest<HttpbinData> request = new BaseHttpRequest.Builder<HttpbinData>().
				setUrl("http://httpbin.org/get").
				setResponseHandler(new ResponseHandler<HttpbinData>(new BodyViaGson<HttpbinData>(HttpbinData.class))).
				build();

		HttpbinData data = HttpClient.parseRequest(request);
		assertNotNull(data);
		assertEquals(request.getUri().toString(), data.url);
	}

	public static class FacebookErrorData {
		private static class ErrorInfo {
			String message;
			String type;
			int code;
		}

		ErrorInfo error;
	}

	public void testGsonErrorData() throws Exception {
		BaseHttpRequest<String> request = new BaseHttpRequest.Builder<String>().
				setUrl("http://graph.facebook.com/test").
				setResponseHandler(
						new ResponseHandler<String>(BodyToString.INSTANCE,
								new HttpFailureHandlerViaXferTransform(
										new BodyViaGson<FacebookErrorData>(FacebookErrorData.class)
								)
						)
				).
				build();

		try {
			String data = HttpClient.parseRequest(request);
			fail("We should never have received data:"+data);
		} catch (HttpFailureException e) {
			Object errorException = e.getParsedError();
			assertTrue(errorException instanceof FacebookErrorData);
			FacebookErrorData errorData = (FacebookErrorData) errorException;
			assertNotNull(errorData.error);
			assertEquals(803, errorData.error.code);
		}
	}

	public void testGsonErrorDebugData() throws Exception {
		BodyViaGson<FacebookErrorData> testParser = new BodyViaGson<FacebookErrorData>(FacebookErrorData.class);
		testParser.enableDebugData(true);
		BaseHttpRequest<String> request = new BaseHttpRequest.Builder<String>().
				setUrl("http://graph.facebook.com/test").
				setResponseHandler(
						new ResponseHandler<String>(BodyToString.INSTANCE,
								new HttpFailureHandlerViaXferTransform(testParser)
						)
				).
				build();

		try {
			String data = HttpClient.parseRequest(request);
			fail("We should never have received data:"+data);
		} catch (HttpFailureException e) {
			Object errorException = e.getParsedError();
			assertTrue(errorException instanceof FacebookErrorData);
			FacebookErrorData errorData = (FacebookErrorData) errorException;
			assertNotNull(errorData.error);
			assertEquals(803, errorData.error.code);
		}
	}

	public void testSetDebugData() throws Exception {
		BodyViaGson<Void> testParser = new BodyViaGson<Void>(Void.class);
		testParser.enableDebugData(true);
		BaseHttpRequest<Void> request = new BaseHttpRequest.Builder<Void>().
				setUrl("http://httpbin.org/ip").
				setResponseHandler(new ResponseHandler<Void>(testParser)).
				build();

		try {
			HttpClient.parseRequest(request);
		} catch (HttpDataParserException e) {
			assertEquals("Bad data for GSON", e.getCause().getMessage());
			assertNotNull(e.getCause().getSourceData());
		}
	}
}