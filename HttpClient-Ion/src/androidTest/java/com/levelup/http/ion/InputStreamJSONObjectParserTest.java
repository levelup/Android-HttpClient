package com.levelup.http.ion;

import org.json.JSONObject;

import android.content.Context;
import android.test.AndroidTestCase;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpResponseHandler;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;
import com.levelup.http.ParserException;
import com.levelup.http.parser.ResponseToJSONObject;

public class InputStreamJSONObjectParserTest extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		IonClient.setup(context);
	}
	
	public void testBogusData() throws Exception {
		BaseHttpRequest<JSONObject> request = new BaseHttpRequest.Builder<JSONObject>().
				setUrl("http://android.com/").
				setResponseParser(new HttpResponseHandler<JSONObject>(ResponseToJSONObject.INSTANCE)).
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