package com.levelup.http.gson;

import com.google.gson.GsonBuilder;
import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;

import junit.framework.TestCase;

public class InputStreamGsonParserTest extends TestCase {

	public void testSetDebugData() throws Exception {
		InputStreamGsonParser<Void> testParser = new InputStreamGsonParser<Void>(new GsonBuilder().create(), Void.class);
		testParser.enableDebugData(true);
		BaseHttpRequest<Void> request = new BaseHttpRequest.Builder<Void>().setUrl("http://android.com/").setStreamParser(testParser).build();

		try {
			HttpClient.parseRequest(request);
		} catch (HttpException e) {
			if (e.getErrorCode()!=HttpException.ERROR_JSON)
				throw e; // forward
			assertNotNull(e.getMessage());
			assertTrue(e.getMessage().startsWith("Bad Json data:"));
		}
	}
}