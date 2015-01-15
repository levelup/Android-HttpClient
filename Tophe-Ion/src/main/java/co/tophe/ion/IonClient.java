package co.tophe.ion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.koushikdutta.async.http.AsyncHttpClientMiddleware;
import com.koushikdutta.async.http.AsyncSSLEngineConfigurator;
import com.koushikdutta.ion.Ion;

import javax.net.ssl.SSLEngine;

import co.tophe.TopheClient;
import co.tophe.engine.HttpEngineFactoryFallback;
import co.tophe.log.LogManager;

/**
 * Setup class for the TOPHE client to use the <a href="https://github.com/koush/ion">Ion</a> engine for all HTTP processing.
 * You must call {@link #setup(android.content.Context)} before using TOPHE with Ion.
 * <p>Live {@link co.tophe.HttpStream HttpStream} is currently not supported by the Ion engine.</p>
 *
 * @author Created by Steve Lhomme on 15/07/2014.
 * @see #setupIon(com.koushikdutta.ion.Ion)
 */
public final class IonClient {

	public static final int PLAY_SERVICES_BOGUS_CONSCRYPT = 5089034; // see https://github.com/koush/AndroidAsync/issues/210
	private static Boolean useConscrypt;
	static Boolean forbidSSL;
	private static Integer conscryptVersion;

	private IonClient() {
	}

	/**
	 * Base setup function that sets Ion as the default {@link co.tophe.HttpEngine}
	 *
	 * @param context
	 */
	public static void setup(@NonNull Context context) {
		IonHttpEngineFactory factory = IonHttpEngineFactory.getInstance(context);
		TopheClient.setHttpEngineFactory(new HttpEngineFactoryFallback(factory, TopheClient.getHttpEngineFactory()));

		//factory.getDefaultIon().configure().setLogging("TOPHE", Log.VERBOSE);

		TopheClient.setup(context);
	}

	/**
	 * Setup the specified Ion engine to work well with TOPHE.
	 * <p>It will disable SSLv3 and use Google's Conscrypt SSL stack from the Play Services if available.</p>
	 * @param ion
	 */
	public static void setupIon(Ion ion) {
		if (null == conscryptVersion) {
			conscryptVersion = 0;
			PackageManager pm = ion.getContext().getPackageManager();
			try {
				PackageInfo pI = pm.getPackageInfo("com.google.android.gms", 0);
				if (pI != null) {
					conscryptVersion = pI.versionCode;
				}
			} catch (PackageManager.NameNotFoundException ignored) {
				try {
					Class<?> conscryptClass = ion.getClass().getClassLoader().loadClass("com.android.org.conscrypt.OpenSSLEngineImpl");
					if (conscryptClass != null) {
						conscryptVersion = PLAY_SERVICES_BOGUS_CONSCRYPT /*CONSCRYPT_LACKS_SNI*/ + 1; // assume everything is fine
					}
				} catch (ClassNotFoundException ignored2) {
				}
			}


			useConscrypt = conscryptVersion > PLAY_SERVICES_BOGUS_CONSCRYPT;
			// dual parallel connection to Feedly results in data never received https://github.com/koush/ion/issues/428
			forbidSSL = false; //useConscrypt && conscryptVersion >= BOGUS_CONSCRYPT_DUAL_FEEDLY;
		}

		ion.getConscryptMiddleware().enable(useConscrypt);

		if (useConscrypt) {
			ion.getConscryptMiddleware().initialize();
		}

		ion.getHttpClient().getSSLSocketMiddleware().addEngineConfigurator(new AsyncSSLEngineConfigurator() {
			@Override
			public void configureEngine(SSLEngine engine, AsyncHttpClientMiddleware.GetSocketData data, String host, int port) {
				if (false /*conscryptVersion > CONSCRYPT_LACKS_SNI || !useConscrypt*/) {
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
					} catch (Exception e) {
						if (engine.getClass().getCanonicalName().contains(".conscrypt.")) {
							if (forbidSSL /*&& conscryptVersion <= CONSCRYPT_LACKS_SNI*/) // we know that Conscrypt version
								LogManager.getLogger().v("Failed to set the flags in " + engine + " conscryptVersion=" + conscryptVersion);
							else
								LogManager.getLogger().w("Failed to set the flags in " + engine + " conscryptVersion=" + conscryptVersion, e);
						} else if (useConscrypt) {
							LogManager.getLogger().e("Failed to set the flags in " + engine + " conscryptVersion=" + conscryptVersion, e);
						} else {
							LogManager.getLogger().i("Failed to set the flags in " + engine + " conscryptVersion=" + conscryptVersion, e);
						}
					}
				}

				// disable SSLv3 except if it's alone
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
	}
}
