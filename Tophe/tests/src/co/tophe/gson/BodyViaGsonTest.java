package co.tophe.gson;

import android.content.Context;
import android.test.AndroidTestCase;

import com.google.gson.annotations.SerializedName;

import co.tophe.BaseHttpRequest;
import co.tophe.BaseResponseHandler;
import co.tophe.TopheClient;
import co.tophe.HttpDataParserException;
import co.tophe.ImmutableHttpRequest;
import co.tophe.ResponseHandler;
import co.tophe.ServerException;
import co.tophe.parser.BodyToString;
import co.tophe.parser.BodyTransformChain;
import co.tophe.parser.XferTransform;

public class BodyViaGsonTest extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		TopheClient.setup(context);
	}

	private static class HttpbinData {
		@SerializedName("url") String url;
	}

	public void testGsonData() throws Exception {
		BaseHttpRequest<HttpbinData,ServerException> request = new BaseHttpRequest.Builder<HttpbinData,ServerException>().
				setUrl("http://httpbin.org/get").
				setResponseHandler(new BaseResponseHandler<HttpbinData>(new BodyViaGson<HttpbinData>(HttpbinData.class))).
				build();

		HttpbinData data = TopheClient.parseRequest(request);
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
		protected FacebookException(ImmutableHttpRequest request, FacebookErrorData facebookErrorData) {
			super(request, facebookErrorData);
		}
	}

	private static final XferTransform<FacebookErrorData, FacebookException> exceptionParser = new XferTransform<FacebookErrorData, FacebookException>() {
		@Override
		public FacebookException transformData(FacebookErrorData facebookErrorData, ImmutableHttpRequest request) {
			return new FacebookException(request, facebookErrorData);
		}
	};

	public void testGsonErrorData() throws Exception {
		BaseHttpRequest<String, FacebookException> request = new BaseHttpRequest.Builder<String, FacebookException>().
				setUrl("https://graph.facebook.com/test").
				setResponseHandler(
						new ResponseHandler<String, FacebookException>(BodyToString.INSTANCE,
								BodyTransformChain.createBuilder(new BodyViaGson<FacebookErrorData>(FacebookErrorData.class))
										.addDataTransform(exceptionParser)
										.build()
						)
				).
				build();

		try {
			String data = TopheClient.parseRequest(request);
			fail("We should never have received data:"+data);
		} catch (FacebookException e) {
			Object serverError = e.getServerError();
			assertNotNull(serverError);
			assertEquals(FacebookErrorData.class, serverError.getClass());
			FacebookErrorData errorData = (FacebookErrorData) serverError;
			assertNotNull(errorData.error);
			assertEquals(803, errorData.error.code);
		}
	}

	public void testGsonErrorDebugData() throws Exception {
		BodyViaGson<FacebookErrorData> testParser = new BodyViaGson<FacebookErrorData>(FacebookErrorData.class);
		testParser.enableDebugData(true);
		BaseHttpRequest<String, FacebookException> request = new BaseHttpRequest.Builder<String, FacebookException>().
				setUrl("https://graph.facebook.com/test").
				setResponseHandler(
						new ResponseHandler<String, FacebookException>(BodyToString.INSTANCE,
								BodyTransformChain.createBuilder(new BodyViaGson<FacebookErrorData>(FacebookErrorData.class))
										.addDataTransform(exceptionParser)
										.build()
						)
				).
				build();

		try {
			String data = TopheClient.parseRequest(request);
			fail("We should never have received data:"+data);
		} catch (FacebookException e) {
			Object serverError = e.getServerError();
			assertNotNull(serverError);
			assertEquals(FacebookErrorData.class, serverError.getClass());
			FacebookErrorData errorData = (FacebookErrorData) serverError;
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
			TopheClient.parseRequest(request);
		} catch (HttpDataParserException e) {
			assertEquals("Bad data for GSON", e.getCause().getMessage());
			assertNotNull(e.getCause().getSourceData());
		}
	}
}