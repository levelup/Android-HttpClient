package com.levelup.http;

import org.json.JSONObject;

import android.content.Context;
import android.test.AndroidTestCase;

public class InputStreamJSONObjectParserTest extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		HttpClient.setup(context);
	}
	
	public void testBogusData() throws Exception {
		BaseHttpRequest<JSONObject> request = new BaseHttpRequest.Builder<JSONObject>().
				setUrl("http://android.com/").
				setStreamParser(InputStreamJSONObjectParser.instance).
				build();

		try {
			HttpClient.parseRequest(request);
		} catch (HttpException e) {
			if (e.getErrorCode() != HttpException.ERROR_PARSER)
				throw e; // forward
			assertNotNull(e.getMessage());
			assertTrue(e.getCause() instanceof ParserException);
			ParserException pe = (ParserException) e.getCause();
			assertTrue(pe.getMessage().equals("Bad JSON data"));
			assertNotNull(pe.getSourceData());
		}
	}
}