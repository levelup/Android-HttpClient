package com.levelup.http.ion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.JsonReader;

import com.levelup.http.ResponseHandler;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;
import com.levelup.http.HttpRequestGet;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;
import com.levelup.http.parser.ResponseTransformChain;
import com.levelup.http.parser.XferTransform;

public class testParser extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		IonClient.setup(context);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void testCustomParser() {
		HttpRequestGet apiGet = new HttpRequestGet("http://social.appxoid.com/json/get_apps_by_pages2");

		try {
			Void parsed = HttpClient.parseRequest(apiGet, new ResponseHandler<Void>(new ResponseTransformChain.Builder<Void>()
					.buildChain(new XferTransform<InputStream, Void>() {
						@Override
						public Void transformData(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException {
							// Process your InputStream
							JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
							try {
								return readMessagesArray(reader);
							} finally {
								reader.close();
							}
						}
					})
			));
		} catch (HttpException e) {
			// shit happens
		}
	}

	private Void readMessagesArray(JsonReader reader) {
		return null;

	}
}
