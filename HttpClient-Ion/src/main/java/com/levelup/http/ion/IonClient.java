package com.levelup.http.ion;

import android.content.Context;

import com.levelup.http.HttpClient;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonClient {

	private IonClient() {
	}

	public static void setup(Context context) {
		HttpClient.setup(context);
		HttpClient.setHttpEngineFactory(new IonHttpEngineFactory(HttpClient.getHttpEngineFactory()));
	}
}
