package co.tophe.ion;

import android.content.Context;

import co.tophe.HttpClient;
import co.tophe.HttpEngineFactory;
import co.tophe.HttpEngineFactoryFallback;

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
