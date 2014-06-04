package com.levelup.http;

import android.net.Uri;
import android.test.AndroidTestCase;

public class TestHTTPClasses extends AndroidTestCase {
	
	public void testUriParamsFromUri() throws Exception {
		Uri test = Uri.parse("https://graph.facebook.com/123146?fields=toto&limit=3");
		new UriParams(test);
	}

}
