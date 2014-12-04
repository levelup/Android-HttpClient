package co.tophe.ion;

import android.content.Context;

import com.koushikdutta.async.http.AsyncSSLEngineConfigurator;
import com.koushikdutta.ion.conscrypt.ConscryptMiddleware;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import co.tophe.HttpEngineFactory;
import co.tophe.HttpEngineFactoryFallback;
import co.tophe.TopheClient;
import co.tophe.log.LogManager;

/**
 * Created by Steve Lhomme on 15/07/2014.
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

		// TODO enable when 1.4.2 is out factory.getDefaultIon().getConscryptMiddleware().initialize(context);

		// disable SSLv3 except if it's alone
		factory.getDefaultIon().getHttpClient().getSSLSocketMiddleware().addEngineConfigurator(new AsyncSSLEngineConfigurator() {
			@Override
			public void configureEngine(SSLEngine engine, String host, int port) {
				// temporary fix for https://github.com/koush/ion/issues/428
				try {
					Field sslParameters = engine.getClass().getDeclaredField("sslParameters");
					Field useSni = sslParameters.getType().getDeclaredField("useSni");
					Field peerHost = engine.getClass().getSuperclass().getDeclaredField("peerHost");
					Field peerPort = engine.getClass().getSuperclass().getDeclaredField("peerPort");

					peerHost.setAccessible(true);
					peerPort.setAccessible(true);
					sslParameters.setAccessible(true);
					useSni.setAccessible(true);

					Object sslp = sslParameters.get(engine);

					peerHost.set(engine, host);
					peerPort.set(engine, port);
					useSni.set(sslp, true);
				}
				catch (Exception e) {
					LogManager.getLogger().i("Failed to set the flags in " + engine, e);
				}

				String[] protocols = engine.getEnabledProtocols();
				if (protocols != null && protocols.length > 1) {
					List<String> enabledProtocols = new ArrayList<String>(Arrays.asList(protocols));
					if (enabledProtocols.remove("SSLv3")) {
						protocols = enabledProtocols.toArray(new String[enabledProtocols.size()]);
						engine.setEnabledProtocols(protocols);
					}
				}
			}
		});

		TopheClient.setup(context);
	}
}
