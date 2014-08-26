package com.levelup.http.ion.gson;

import android.content.Context;
import android.test.AndroidTestCase;

import com.google.gson.annotations.SerializedName;
import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;
import com.levelup.http.HttpResponseHandler;
import com.levelup.http.ParserException;
import com.levelup.http.gson.ResponseViaGson;
import com.levelup.http.ion.IonClient;

public class ResponseViaGsonTest extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		IonClient.setup(context);
	}

	private static class HttpbinData {
		@SerializedName("url") String url;
	}

	public void testGsonData() throws Exception {
		BaseHttpRequest<HttpbinData> request = new BaseHttpRequest.Builder<HttpbinData>().
				setUrl("http://httpbin.org/get").
				setResponseParser(new HttpResponseHandler<HttpbinData>(new ResponseViaGson<HttpbinData>(HttpbinData.class))).
				build();

		HttpbinData data = HttpClient.parseRequest(request);
		assertNotNull(data);
		assertEquals(request.getUri().toString(), data.url);
	}

	public void testSetDebugData() throws Exception {
		ResponseViaGson<Void> testParser = new ResponseViaGson<Void>(Void.class);
		testParser.enableDebugData(true);
		BaseHttpRequest<Void> request = new BaseHttpRequest.Builder<Void>().
				setUrl("http://www.google.com/").
				setResponseParser(new HttpResponseHandler<Void>(testParser)).
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