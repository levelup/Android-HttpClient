package co.tophe.ion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.koushikdutta.async.http.AsyncSSLEngineConfigurator;
import com.koushikdutta.ion.Ion;

import javax.net.ssl.SSLEngine;

import co.tophe.DummyHttpEngine;
import co.tophe.HttpEngine;
import co.tophe.HttpEngineFactory;
import co.tophe.HttpResponse;
import co.tophe.ResponseHandler;
import co.tophe.ServerException;
import co.tophe.log.LogManager;
import co.tophe.parser.Utils;
import co.tophe.parser.XferTransform;
import co.tophe.parser.XferTransformChain;
import co.tophe.parser.XferTransformInputStreamHttpStream;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonHttpEngineFactory implements HttpEngineFactory {

	private static IonHttpEngineFactory INSTANCE;
	public static final int PLAY_SERVICES_BOGUS_CONSCRYPT = 5089034; // see https://github.com/koush/AndroidAsync/issues/210
	public static final int BOGUS_CONSCRYPT_DUAL_FEEDLY = 6587000; // see https://github.com/koush/ion/issues/443
	public static final int CONSCRYPT_LACKS_SNI = 6587038; // 6587030 to 6587038 don't have it

	private final Ion ion;

	public static IonHttpEngineFactory getInstance(Context context) {
		if (null == INSTANCE) {
			INSTANCE = new IonHttpEngineFactory(context);
		}
		return INSTANCE;
	}

	private IonHttpEngineFactory(Context context) {
		if (context == null) {
			throw new NullPointerException("Ion HTTP request with no Context");
		}

		ion = Ion.getDefault(context);
		setupIon(ion);
	}

	private static Boolean useConscrypt;
	private static Boolean forbidSSL;
	private static Integer conscryptVersion;

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
						conscryptVersion = CONSCRYPT_LACKS_SNI + 1; // assume everything is fine
					}
				} catch (ClassNotFoundException ignored2) {
				}
			}


			useConscrypt = conscryptVersion > PLAY_SERVICES_BOGUS_CONSCRYPT;
			// dual parallel connection to Feedly results in data never received https://github.com/koush/ion/issues/428
			forbidSSL = useConscrypt && conscryptVersion >= BOGUS_CONSCRYPT_DUAL_FEEDLY;
		}

		ion.getConscryptMiddleware().enable(useConscrypt);

		if (useConscrypt) {
			// TODO enable when 1.4.2 is out ion.getConscryptMiddleware().initialize(context);
			//ion.getConscryptMiddleware().initialize();
		}

		ion.getHttpClient().getSSLSocketMiddleware().addEngineConfigurator(new AsyncSSLEngineConfigurator() {
			@Override
			public void configureEngine(SSLEngine engine, String host, int port) {
				if (conscryptVersion > CONSCRYPT_LACKS_SNI || !useConscrypt) {
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
							if (forbidSSL && conscryptVersion < CONSCRYPT_LACKS_SNI) // we know that Conscrypt version
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

	@NonNull
	public Ion getDefaultIon() {
		return ion;
	}

	@Override
	public <T, SE extends ServerException> HttpEngine<T, SE> createEngine(HttpEngine.Builder<T, SE> builder) {
		return createEngine(builder, ion, false);
	}

	/**
	 *
	 * @param builder
	 * @param ion
	 * @param allowBogusSSL Sometimes Ion maybe have problems with SSL, especially with Conscrypt, but you may decide to take the risk anyway and use it in conditions where it may fail
	 * @param <T>
	 * @param <SE>
	 * @return
	 */
	public <T, SE extends ServerException> HttpEngine<T,SE> createEngine(HttpEngine.Builder<T,SE> builder, Ion ion, boolean allowBogusSSL) {
		if (!allowBogusSSL && forbidSSL && "https".equals(builder.getHttpRequest().getUri().getScheme())) {
			return new DummyHttpEngine<T,SE>(builder);
		}

		if (!canHandleXferTransform(builder.getResponseHandler().contentParser))
			return new DummyHttpEngine<T,SE>(builder);

		if (!errorCompatibleWithData(builder.getResponseHandler()))
			// Ion returns the data fully parsed so if we don't have common ground to parse the data and the error data, Ion can't handle the request
			return new DummyHttpEngine<T,SE>(builder);

		return new HttpEngineIon<T,SE>(builder, ion);
	}

	private static <T> boolean canHandleXferTransform(XferTransform<HttpResponse, T> contentParser) {
		if (contentParser instanceof XferTransformChain) {
			XferTransformChain<HttpResponse, T> parser = (XferTransformChain<HttpResponse, T>) contentParser;
			for (XferTransform transform : parser.transforms) {
				if (transform == XferTransformInputStreamHttpStream.INSTANCE)
					return false;
			}
		}
		return true;
	}

	/**
	 * See if we can find common ground to parse the data and the error data inside Ion
	 * @param responseHandler
	 * @return whether Ion will be able to parse the data and the error in its processing thread
	 */
	private static boolean errorCompatibleWithData(ResponseHandler<?,?> responseHandler) {
		return Utils.getCommonXferTransform(responseHandler.contentParser, responseHandler.errorParser, false) != null;
	}
}
