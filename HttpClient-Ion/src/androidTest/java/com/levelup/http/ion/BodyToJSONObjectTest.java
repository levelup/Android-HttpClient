package com.levelup.http.ion;

import org.json.JSONObject;

import android.content.Context;
import android.test.AndroidTestCase;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpDataParserException;
import com.levelup.http.ServerException;
import com.levelup.http.parser.BodyToJSONObject;
import com.levelup.http.parser.ParserException;

public class BodyToJSONObjectTest extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		IonClient.setup(context);
	}
	
	public void testBogusData() throws Exception {
		BaseHttpRequest<JSONObject, ServerException> request = new BaseHttpRequest.Builder<JSONObject, ServerException>().
				setUrl("http://android.com/").
				setResponseHandler(BodyToJSONObject.RESPONSE_HANDLER).
				build();

		try {
			HttpClient.parseRequest(request);
		} catch (HttpDataParserException e) {
			assertNotNull(e.getMessage());
			assertTrue(e.getCause().getMessage().startsWith("Bad JSON data"));
			assertNotNull(e.getCause().getSourceData());
		}
	}
}