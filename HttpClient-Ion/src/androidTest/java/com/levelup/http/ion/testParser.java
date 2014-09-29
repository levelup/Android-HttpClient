package com.levelup.http.ion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.JsonReader;

import com.levelup.http.BaseResponseHandler;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;
import com.levelup.http.HttpRequestGet;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ServerException;
import com.levelup.http.parser.BodyTransformChain;
import com.levelup.http.parser.ParserException;
import com.levelup.http.parser.XferTransform;

public class testParser extends AndroidTestCase {

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		IonClient.setup(context);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void testCustomParser() throws ServerException {
		HttpRequestGet<Void> apiGet = new HttpRequestGet<Void>("http://social.appxoid.com/json/get_apps_by_pages2",
				new BaseResponseHandler<Void>(
						new BodyTransformChain<Void>(new XferTransform<InputStream, Void>() {
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

		try {
			Void parsed = HttpClient.parseRequest(apiGet);
		} catch (HttpException e) {
			// shit happens
		}
	}

	private Void readMessagesArray(JsonReader reader) {
		return null;

	}
}
