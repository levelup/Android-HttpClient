package co.tophe.ion;

import org.json.JSONObject;

import android.content.Context;
import android.test.AndroidTestCase;

import co.tophe.BaseHttpRequest;
import co.tophe.TopheClient;
import co.tophe.HttpDataParserException;
import co.tophe.ServerException;
import co.tophe.parser.BodyToJSONObject;

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
			TopheClient.parseRequest(request);
		} catch (HttpDataParserException e) {
			assertNotNull(e.getMessage());
			assertTrue(e.getCause().getMessage().startsWith("Bad JSON data"));
			assertNotNull(e.getCause().getSourceData());
		}
	}
}