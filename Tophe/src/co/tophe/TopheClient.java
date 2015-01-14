package co.tophe;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import co.tophe.parser.BodyToString;

/**
 * HTTP client that handles {@link HttpRequest}
 */
public class TopheClient {

	private static String userAgent;
	private static String xRequestedWith;
	private static CookieManager cookieManager;
	private static Header[] defaultHeaders;
	private static HttpEngineFactory httpEngineFactory = BaseHttpEngineFactory.INSTANCE;

	//public static final int PLAY_SERVICES_BOGUS_SSLV3 = 6183070;

	private static Boolean useConscrypt;

	/**
	 * Setup internal values of the {@link TopheClient} using the provided {@link Context}
	 * <p>The user agent is deduced from the app name of the {@code context} if it's not {@code null}</p>
	 *
	 * @param context Used to get a proper User Agent for your app, may be {@code null}
	 */
	public static void setup(Context context) {
		if (null != context) {
			ApplicationInfo applicationInfo = context.getApplicationInfo();
			xRequestedWith = applicationInfo.packageName;
			int versionCode = -1;
			PackageManager pM = context.getPackageManager();
			try {
				PackageInfo pI = pM.getPackageInfo(context.getPackageName(), 0);
				if (pI != null) {
					versionCode = pI.versionCode;
				}
			} catch (NameNotFoundException ignored) {
			} finally {
				if (TextUtils.isEmpty(applicationInfo.nonLocalizedLabel))
					userAgent = applicationInfo.packageName + "/" + versionCode;
				else
					userAgent = applicationInfo.nonLocalizedLabel + "/" + versionCode;
			}

			if (null == useConscrypt) {
				useConscrypt = true;
				/* The Play Services are are bogus on old versions, see https://code.google.com/p/android/issues/detail?id=78187
				try {
					PackageInfo pI = pM.getPackageInfo("com.google.android.gms", 0);
					if (pI != null) {
						useConscrypt = pI.versionCode > PLAY_SERVICES_BOGUS_SSLV3;
					}
				} catch (PackageManager.NameNotFoundException ignored) {
				}

				if (useConscrypt)*/
				{
					try {
						Class<?> providerInstaller = Class.forName("com.google.android.gms.security.ProviderInstaller");
						Method mInsertProvider = providerInstaller.getDeclaredMethod("installIfNeeded", Context.class);
						mInsertProvider.invoke(null, context);

					} catch (Throwable ignored) {
						try {
							Context gms = context.createPackageContext("com.google.android.gms", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
							Class clazz = gms.getClassLoader().loadClass("com.google.android.gms.common.security.ProviderInstallerImpl");
							Method mInsertProvider = clazz.getDeclaredMethod("insertProvider", Context.class);
							mInsertProvider.invoke(null, context);
						} catch (Throwable e) {
						}
					}
				}
			}

			BaseHttpEngineFactory.INSTANCE.init();
		}
	}

	/**
	 * Get the default user agent set on all requests, unless it's overridden. The value is set during {@link #setup(android.content.Context)}.
	 */
	public static String getUserAgent() {
		return userAgent;
	}

	/**
	 * Get the Android package set in all requests with the {@code X-Requested-With} header similar to what Chrome does. The value is set during {@link #setup(android.content.Context)}.
	 */
	public static String getXRequestedWith() {
		return xRequestedWith;
	}

	/**
	 * @param cookieManager the handler for all cookies received/sent.
	 */
	public static void setCookieManager(@Nullable CookieManager cookieManager) {
		TopheClient.cookieManager = cookieManager;
	}

	/**
	 * Get the handler of all cookies received/sent.
	 */
	@Nullable
	public static CookieManager getCookieManager() {
		return cookieManager;
	}

	/**
	 * Set some default headers to set on all HTTP requests, eg DNT=1.
	 */
	public static void setDefaultHeaders(@Nullable Header[] headers) {
		defaultHeaders = headers;
	}

	/**
	 * Get the default headers that will be set on all HTTP requests.
	 */
	@Nullable
	public static Header[] getDefaultHeaders() {
		return defaultHeaders;
	}

	/**
	 * Helper function to read the response of the HTTP request as a {@code String}.
	 *
	 * @param request The HTTP request to process
	 * @return a {@code String} version of the data returned by the server.
	 * @throws ServerException if the server didn't like the request.
	 * @throws HttpException   if there was an error other than a server error.
	 */
	public static String getStringResponse(HttpRequest request) throws ServerException, HttpException {
		return new HttpEngine.Builder<String, ServerException>()
				.setRequest(request)
				.setResponseHandler(BodyToString.RESPONSE_HANDLER)
				.build()
				.call();
	}

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of a problem</p>
	 *
	 * @param request the HTTP request to process.
	 * @return The parsed object or null
	 * @throws SE            if the server didn't like the request.
	 * @throws HttpException if there was an error other than a server error.
	 * @see co.tophe.async.AsyncTopheClient#postRequest(TypedHttpRequest, co.tophe.async.AsyncCallback) AsyncTopheClient.postRequest()
	 * to get the response object asynchronously
	 */
	public static <T, SE extends ServerException> T parseRequest(TypedHttpRequest<T, SE> request) throws SE, HttpException {
		return new HttpEngine.Builder<T, SE>()
				.setTypedRequest(request)
				.build()
				.call();
	}

	/**
	 * Get the default HTTP engine factory.
	 */
	public static HttpEngineFactory getHttpEngineFactory() {
		return httpEngineFactory;
	}

	/**
	 * Set the default HTTP engine factory.
	 *
	 * @see co.tophe.BaseHttpEngineFactory BaseHttpEngineFactory uses URLConnection to handle HTTP requests.
	 * @see co.tophe.ion.IonHttpEngineFactory IonHttpEngineFactory uses Ion to handle HTTP requests.
	 */
	public static void setHttpEngineFactory(HttpEngineFactory httpEngineFactory) {
		TopheClient.httpEngineFactory = httpEngineFactory;
	}
}
