package com.levelup.http;

import org.json.JSONObject;

import android.content.Context;
import android.test.AndroidTestCase;

import com.levelup.http.parser.BodyToJSONObject;
import com.levelup.http.parser.ParserException;

public class ResponseToJSONObjectTest extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		HttpClient.setup(context);
	}
	
	public void testBogusData() throws Exception {
		BaseHttpRequest<JSONObject> request = new BaseHttpRequest.Builder<JSONObject>().
				setUrl("http://android.com/").
				setResponseHandler(BodyToJSONObject.RESPONSE_HANDLER).
				build();

		try {
			HttpClient.parseRequest(request);
		} catch (HttpException e) {
			assertEquals(HttpException.ERROR_PARSER, e.getErrorCode());
			assertNotNull(e.getMessage());
			assertTrue(e.getCause() instanceof ParserException);
			ParserException pe = (ParserException) e.getCause();
			assertTrue(pe.getMessage().startsWith("Bad JSON data"));
			assertNotNull(pe.getSourceData());
		}
	}
}