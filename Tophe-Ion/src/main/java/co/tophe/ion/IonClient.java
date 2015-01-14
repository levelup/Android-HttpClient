package co.tophe.ion;

import android.content.Context;

import co.tophe.HttpEngineFactory;
import co.tophe.engine.HttpEngineFactoryFallback;
import co.tophe.TopheClient;

/**
 * @author Created by Steve Lhomme on 15/07/2014.
 */
public class IonClient {

	private IonClient() {
	}

	public static void setup(Context context) {
		setup(context, TopheClient.getHttpEngineFactory());
	}

	public static void setup(Context context, HttpEngineFactory fallbackFactory) {
		IonHttpEngineFactory factory = IonHttpEngineFactory.getInstance(context);
		TopheClient.setHttpEngineFactory(new HttpEngineFactoryFallback(factory, fallbackFactory));

		//factory.getDefaultIon().configure().setLogging("TOPHE", Log.VERBOSE);

		TopheClient.setup(context);
	}
}
