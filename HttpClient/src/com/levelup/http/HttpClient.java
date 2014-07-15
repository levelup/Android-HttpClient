package com.levelup.http;

import java.io.InputStream;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * HTTP client that handles {@link HttpRequest} 
 */
public class HttpClient {
	public static final String ACCEPT_ENCODING = "Accept-Encoding";

	private static String userAgent;
	private static CookieManager cookieManager;
	private static Header[] defaultHeaders;
	private static HttpEngineFactory httpEngineFactory = BaseHttpEngineFactory.instance;
	public static Context defaultContext;

	/**
	 * Setup internal values of the {@link HttpClient} using the provided {@link Context}
	 * <p>The user agent is deduced from the app name of the {@code context} if it's not {@code null}</p>
	 * @param context Used to get a proper User Agent for your app, may be {@code null}
	 */
	public static void setup(Context context) {
		if (null!=context) {
			defaultContext = context;
			PackageManager pM = defaultContext.getPackageManager();
			try {
				PackageInfo pI = pM.getPackageInfo(defaultContext.getPackageName(), 0);
				if (pI != null)
					userAgent = pI.applicationInfo.nonLocalizedLabel + "/" + pI.versionCode;
			} catch (NameNotFoundException ignored) {
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

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @return The InputStream corresponding to the data stream, may be null
	 * @throws HttpException
	 */
	public static InputStream getInputStream(HttpRequest request) throws HttpException {
		if (request instanceof BaseHttpRequest) {
			BaseHttpRequest baseHttpRequest = (BaseHttpRequest) request;
			HttpEngine httpEngine = baseHttpRequest.getHttpEngine();
			return httpEngine.getInputStream(request);
		}

		return null;
	}

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @return The parsed object or null
	 * @throws HttpException
	 */
	public static <T> T parseRequest(TypedHttpRequest<T> request) throws HttpException {
		InputStreamParser<T> streamParser = request.getInputStreamParser();
		if (null==streamParser) throw new NullPointerException("typed request without a stream parser:"+request);
		return parseRequest(request, streamParser);
	}

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @param parser The {@link InputStreamParser parser} used to transform the input stream into the desired type. May be {@code null}
	 * @return The parsed object or null
	 * @throws HttpException
	 */
	public static <T> T parseRequest(final HttpRequest request, InputStreamParser<T> parser) throws HttpException {
		if (request instanceof BaseHttpRequest) {
			BaseHttpRequest baseHttpRequest = (BaseHttpRequest) request;
			HttpEngine httpEngine = baseHttpRequest.getHttpEngine();
			return (T) httpEngine.parseRequest(parser, request);
		}

		return null;
	}

	/**
	 * Perform the query on the network and get the resulting body as a String
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @return The resulting body as a String
	 * @throws HttpException
	 */
	public static String getStringResponse(HttpRequest request) throws HttpException {
		return parseRequest(request, InputStreamStringParser.instance);
	}

	public static HttpEngineFactory getHttpEngineFactory() {
		return httpEngineFactory;
	}

	public static void setHttpEngineFactory(HttpEngineFactory httpEngineFactory) {
		HttpClient.httpEngineFactory = httpEngineFactory;
	}
}
