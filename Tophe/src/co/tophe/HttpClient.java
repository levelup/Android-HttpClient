package co.tophe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import co.tophe.parser.BodyToString;

/**
 * HTTP client that handles {@link HttpRequest} 
 */
public class HttpClient {

	private static String userAgent;
	private static String xRequestedWith;
	private static CookieManager cookieManager;
	private static Header[] defaultHeaders;
	private static HttpEngineFactory httpEngineFactory = BaseHttpEngineFactory.INSTANCE;

	public static final int PLAY_SERVICES_BOGUS_SSLV3 = 6174070;

	private static Boolean useConscrypt;

	/**
	 * Setup internal values of the {@link HttpClient} using the provided {@link Context}
	 * <p>The user agent is deduced from the app name of the {@code context} if it's not {@code null}</p>
	 * @param context Used to get a proper User Agent for your app, may be {@code null}
	 */
	public static void setup(Context context) {
		if (null!=context) {
			ApplicationInfo applicationInfo = context.getApplicationInfo();
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
				xRequestedWith = applicationInfo.packageName;
			}

			if (null == useConscrypt) {
				useConscrypt = true;
				// The Play Services are are bogus on old versions, see https://android-review.googlesource.com/#/c/99698/
				try {
					PackageInfo pI = pM.getPackageInfo("com.google.android.gms", 0);
					if (pI != null) {
						useConscrypt = pI.versionCode != PLAY_SERVICES_BOGUS_SSLV3;
					}
				} catch (PackageManager.NameNotFoundException ignored) {
				}

				if (useConscrypt) {
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
		}
	}

	public static String getUserAgent() {
		return userAgent;
	}

	public static String getXRequestedWith() {
		return xRequestedWith;
	}

	public static void setCookieManager(CookieManager cookieManager) {
		HttpClient.cookieManager = cookieManager;
	}

	public static CookieManager getCookieManager() {
		return cookieManager;
	}

	public static void setDefaultHeaders(Header[] headers) {
		defaultHeaders = headers;
	}

	public static Header[] getDefaultHeaders() {
		return defaultHeaders;
	}

	public static String getStringResponse(HttpRequest request) throws HttpException, ServerException {
		return new HttpEngine.Builder<String, ServerException>()
				.setRequest(request)
				.setResponseHandler(BodyToString.RESPONSE_HANDLER)
				.build()
				.call();
	}

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @return The parsed object or null
	 * @throws HttpException
	 */
	public static <T, SE extends ServerException> T parseRequest(TypedHttpRequest<T, SE> request) throws HttpException, SE {
		return new HttpEngine.Builder<T, SE>()
				.setTypedRequest(request)
				.build()
				.call();
	}

	public static HttpEngineFactory getHttpEngineFactory() {
		return httpEngineFactory;
	}

	public static void setHttpEngineFactory(HttpEngineFactory httpEngineFactory) {
		HttpClient.httpEngineFactory = httpEngineFactory;
	}
}
