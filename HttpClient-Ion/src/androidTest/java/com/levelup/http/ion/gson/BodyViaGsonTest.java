package com.levelup.http.ion.gson;

import android.content.Context;
import android.test.AndroidTestCase;

import com.google.gson.annotations.SerializedName;
import com.levelup.http.BaseHttpRequest;
import com.levelup.http.BaseResponseHandler;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpDataParserException;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ResponseHandler;
import com.levelup.http.ServerException;
import com.levelup.http.ServerExceptionFactory;
import com.levelup.http.gson.BodyViaGson;
import com.levelup.http.ion.IonClient;
import com.levelup.http.parser.BodyToString;

public class BodyViaGsonTest extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		IonClient.setup(context);
	}

	private static class HttpbinData {
		@SerializedName("url") String url;
	}

	public void testGsonData() throws Exception {
		BaseHttpRequest<HttpbinData,ServerException> request = new BaseHttpRequest.Builder<HttpbinData,ServerException>().
				setUrl("http://httpbin.org/get").
				setResponseHandler(new BaseResponseHandler<HttpbinData>(new BodyViaGson<HttpbinData>(HttpbinData.class))).
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

	private static class FacebookException extends ServerException {

		protected FacebookException(Builder builder) {
			super(builder);
		}

		private static class Builder extends ServerException.Builder {

			public Builder(ImmutableHttpRequest request, Object parsedError) {
				super(request, parsedError);
			}

			@Override
			public FacebookException build() {
				return new FacebookException(this);
			}
		}
	}

	private static final ServerExceptionFactory<FacebookErrorData, FacebookException> exceptionFactory = new ServerExceptionFactory<FacebookErrorData, FacebookException>() {
		@Override
		public FacebookException createException(ImmutableHttpRequest request, FacebookErrorData facebookErrorData) {
			return new FacebookException.Builder(request, facebookErrorData).build();
		}
	};

	public void testGsonErrorData() throws Exception {
		BaseHttpRequest<String, FacebookException> request = new BaseHttpRequest.Builder<String, FacebookException>().
				setUrl("http://graph.facebook.com/test").
				setResponseHandler(
						new ResponseHandler<String, FacebookException>(BodyToString.INSTANCE, new BodyViaGson<FacebookErrorData>(FacebookErrorData.class), exceptionFactory)
				).
				build();

		try {
			String data = HttpClient.parseRequest(request);
			fail("We should never have received data:"+data);
		} catch (FacebookException e) {
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
		BaseHttpRequest<String, FacebookException> request = new BaseHttpRequest.Builder<String, FacebookException>().
				setUrl("http://graph.facebook.com/test").
				setResponseHandler(
						new ResponseHandler<String, FacebookException>(BodyToString.INSTANCE, new BodyViaGson<FacebookErrorData>(FacebookErrorData.class), exceptionFactory)
				).
				build();

		try {
			String data = HttpClient.parseRequest(request);
			fail("We should never have received data:"+data);
		} catch (FacebookException e) {
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
		BaseHttpRequest<Void,ServerException> request = new BaseHttpRequest.Builder<Void,ServerException>().
				setUrl("http://httpbin.org/ip").
				setResponseHandler(new BaseResponseHandler<Void>(testParser)).
				build();

		try {
			HttpClient.parseRequest(request);
		} catch (HttpDataParserException e) {
			assertEquals("Bad data for GSON", e.getCause().getMessage());
			assertNotNull(e.getCause().getSourceData());
		}
	}
}