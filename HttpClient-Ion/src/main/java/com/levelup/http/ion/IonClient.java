package com.levelup.http.ion;

import android.content.Context;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpEngineFactory;
import com.levelup.http.HttpEngineFactoryFallback;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonClient {

	private IonClient() {
	}

	public static void setup(Context context) {
		setup(context, HttpClient.getHttpEngineFactory());
	}

	public static void setup(Context context, HttpEngineFactory fallbackFactory) {
		HttpClient.setup(context);
		HttpClient.setHttpEngineFactory(new HttpEngineFactoryFallback(IonHttpEngineFactory.getInstance(context), fallbackFactory));
	}
}
