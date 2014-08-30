package com.levelup.http;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import com.levelup.http.parser.ResponseToString;

/**
 * HTTP client that handles {@link HttpRequest} 
 */
public class HttpClient {
	public static final String ACCEPT_ENCODING = "Accept-Encoding";

	private static String userAgent;
	private static CookieManager cookieManager;
	private static Header[] defaultHeaders;
	private static HttpEngineFactory httpEngineFactory = BaseHttpEngineFactory.instance;
	static Context defaultContext;

	/**
	 * Setup internal values of the {@link HttpClient} using the provided {@link Context}
	 * <p>The user agent is deduced from the app name of the {@code context} if it's not {@code null}</p>
	 * @param context Used to get a proper User Agent for your app, may be {@code null}
	 */
	public static void setup(Context context) {
		if (null!=context) {
			defaultContext = context;

			ApplicationInfo applicationInfo = context.getApplicationInfo();
			int versionCode = -1;
			PackageManager pM = defaultContext.getPackageManager();
			try {
				PackageInfo pI = pM.getPackageInfo(defaultContext.getPackageName(), 0);
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
		}
	}

	public static String getUserAgent() {
		return userAgent;
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

	public static String getStringResponse(HttpRequest request) throws HttpException {
		return new HttpEngine.Builder<String>()
				.setRequest(request)
				.setResponseHandler(ResponseToString.RESPONSE_HANDLER)
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
	public static <T> T parseRequest(TypedHttpRequest<T> request) throws HttpException {
		return new HttpEngine.Builder<T>()
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
