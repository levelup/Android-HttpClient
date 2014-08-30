package com.levelup.http.ion;

import android.content.Context;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpEngineFactory;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonClient {

	private IonClient() {
	}

	public static IonHttpEngineFactory ION_FACTORY;

	public static void setup(Context context) {
		setup(context, HttpClient.getHttpEngineFactory());
	}

	public static void setup(Context context, HttpEngineFactory fallbackFactory) {
		HttpClient.setup(context);
		ION_FACTORY = new IonHttpEngineFactory(fallbackFactory);
		HttpClient.setHttpEngineFactory(ION_FACTORY);
	}
}
