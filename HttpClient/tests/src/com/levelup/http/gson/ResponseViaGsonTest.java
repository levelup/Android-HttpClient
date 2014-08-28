package com.levelup.http.gson;

import android.content.Context;
import android.test.AndroidTestCase;

import com.google.gson.annotations.SerializedName;
import com.levelup.http.BaseHttpRequest;
import com.levelup.http.DataErrorException;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;
import com.levelup.http.ResponseHandler;
import com.levelup.http.ParserException;
import com.levelup.http.parser.ErrorHandlerParser;
import com.levelup.http.parser.ResponseToString;

public class ResponseViaGsonTest extends AndroidTestCase {

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
				setResponseParser(new ResponseHandler<HttpbinData>(new ResponseViaGson<HttpbinData>(HttpbinData.class))).
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
				setResponseParser(
						new ResponseHandler<String>(ResponseToString.INSTANCE,
								new ErrorHandlerParser(
										new ResponseViaGson<FacebookErrorData>(FacebookErrorData.class)
								)
						)
				).
				build();

		try {
			String data = HttpClient.parseRequest(request);
			fail("We should never have received data:"+data);
		} catch (HttpException e) {
			if (e.getErrorCode()!=HttpException.ERROR_DATA_MSG)
				throw e; // forward
			assertTrue(e.getCause() instanceof DataErrorException);
			DataErrorException errorException = (DataErrorException) e.getCause();
			assertTrue(errorException.errorContent instanceof FacebookErrorData);
			FacebookErrorData errorData = (FacebookErrorData) errorException.errorContent;
			assertNotNull(errorData.error);
			assertEquals(803, errorData.error.code);
		}
	}

	public void testGsonErrorDebugData() throws Exception {
		ResponseViaGson<FacebookErrorData> testParser = new ResponseViaGson<FacebookErrorData>(FacebookErrorData.class);
		testParser.enableDebugData(true);
		BaseHttpRequest<String> request = new BaseHttpRequest.Builder<String>().
				setUrl("http://graph.facebook.com/test").
				setResponseParser(
						new ResponseHandler<String>(ResponseToString.INSTANCE,
								new ErrorHandlerParser(testParser)
						)
				).
				build();

		try {
			String data = HttpClient.parseRequest(request);
			fail("We should never have received data:"+data);
		} catch (HttpException e) {
			if (e.getErrorCode()!=HttpException.ERROR_DATA_MSG)
				throw e; // forward
			assertTrue(e.getCause() instanceof DataErrorException);
			DataErrorException errorException = (DataErrorException) e.getCause();
			assertTrue(errorException.errorContent instanceof FacebookErrorData);
			FacebookErrorData errorData = (FacebookErrorData) errorException.errorContent;
			assertNotNull(errorData.error);
			assertEquals(803, errorData.error.code);
		}
	}

	public void testSetDebugData() throws Exception {
		ResponseViaGson<Void> testParser = new ResponseViaGson<Void>(Void.class);
		testParser.enableDebugData(true);
		BaseHttpRequest<Void> request = new BaseHttpRequest.Builder<Void>().
				setUrl("http://httpbin.org/ip").
				setResponseParser(new ResponseHandler<Void>(testParser)).
				build();

		try {
			HttpClient.parseRequest(request);
		} catch (HttpException e) {
			if (e.getErrorCode()!=HttpException.ERROR_PARSER)
				throw e; // forward
			assertTrue(e.getCause() instanceof ParserException);
			ParserException pe = (ParserException) e.getCause();
			assertEquals("Bad data for GSON", pe.getMessage());
			assertNotNull(pe.getSourceData());
		}
	}
}