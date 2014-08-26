package com.levelup.http.gson;

import android.content.Context;
import android.test.AndroidTestCase;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpResponseHandler;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;
import com.levelup.http.ParserException;

public class ResponseViaGsonTest extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		HttpClient.setup(context);
	}

	private static class HttpbinData {
		@SerializedName("url") String url;
	}

	public void testSetDebugData() throws Exception {
		ResponseViaGson<HttpbinData> testParser = new ResponseViaGson<HttpbinData>(HttpbinData.class);
		testParser.enableDebugData(true);
		BaseHttpRequest<HttpbinData> request = new BaseHttpRequest.Builder<HttpbinData>().
				setUrl("http://httpbin.org/get").
				setResponseParser(new HttpResponseHandler<HttpbinData>(testParser)).
				build();

		try {
			HttpbinData data = HttpClient.parseRequest(request);
			assertNotNull(data);
			assertEquals(request.getUri().toString(), data.url);
		} catch (HttpException e) {
			if (e.getErrorCode()!=HttpException.ERROR_PARSER)
				throw e; // forward
			assertTrue(e.getCause() instanceof ParserException);
			ParserException pe = (ParserException) e.getCause();
			assertTrue(pe.getMessage().equals("Bad Json data"));
			assertNotNull(pe.getSourceData());
		}
	}
}