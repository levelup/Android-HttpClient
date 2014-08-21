package com.levelup.http.gson;

import android.content.Context;
import android.test.AndroidTestCase;

import com.google.gson.GsonBuilder;
import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;
import com.levelup.http.ParserException;
import com.levelup.http.parser.ResponseParser;

public class ResponseToGsonTest extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		HttpClient.setup(context);
	}
	
	public void testSetDebugData() throws Exception {
		ResponseToGson<Void> testParser = new ResponseToGson<Void>(new GsonBuilder().create(), Void.class);
		testParser.enableDebugData(true);
		BaseHttpRequest<Void> request = new BaseHttpRequest.Builder<Void>().
				setUrl("http://android.com/").
				setResponseParser(new ResponseParser<Void, Object>(testParser)).
				build();

		try {
			HttpClient.parseRequest(request);
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