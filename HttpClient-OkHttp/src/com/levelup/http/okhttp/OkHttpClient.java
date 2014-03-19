package com.levelup.http.okhttp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpUrlConnectionFactory;

/**
 * {@link HttpClient} class that uses <a href="http://square.github.io/okhttp/">OkHttp</a> for all connection handling
 */
public class OkHttpClient extends HttpClient implements HttpUrlConnectionFactory {
	/** Flag to enable/disable usage of OkHttp */
	private static final boolean __WITH_OKHTTP = true;

	private static final long HTTP_KEEP_ALIVE = 1 * 60 * 1000; // 1 minute

	private static final com.squareup.okhttp.OkHttpClient okClient;
	public static final OkHttpClient instance;

	private final HashSet<String> urlSpdyBlackList = new HashSet<String>(); 

	static {
		instance = new OkHttpClient();
		if (__WITH_OKHTTP) {
			okClient = new com.squareup.okhttp.OkHttpClient();
			System.setProperty("http.keepAliveDuration", String.valueOf(HTTP_KEEP_ALIVE));
		} else {
			okClient = null;
		}
	}

	public static boolean usesOkHttp() {
		return null != okClient;
	}

	private OkHttpClient() {}


	/**
	 * Add a URL part that should not use SPDY
	 * <p>You may also use {@code setHeader("X-Android-Transports", "http/1.1")} on your {@link com.levelup.http.HttpRequest#setHeader(String, String) HttpRequest}</p>
	 */
	public static void addUrlBlacklist(String urlPart) {
		synchronized (instance.urlSpdyBlackList) {
			instance.urlSpdyBlackList.add(urlPart);
		}
	}

	/**
	 * Remove a URL part from the SPDY blacklist
	 */
	public static void removeUrlBlacklist(String urlPart) {
		synchronized (instance.urlSpdyBlackList) {
			instance.urlSpdyBlackList.remove(urlPart);
		}
	}

	@Override
	public HttpURLConnection createConnection(URL url) throws IOException {
		if (null == okClient)
			return (HttpURLConnection) url.openConnection();

		synchronized (urlSpdyBlackList) {
			if (!urlSpdyBlackList.isEmpty()) {
				String urlString = url.toExternalForm();
				for (String blacklistURL : urlSpdyBlackList) {
					if (urlString.contains(blacklistURL)) {
						//result.setRequestProperty("X-Android-Transports", "http/1.1");
						return (HttpURLConnection) url.openConnection();
					}
				}
			}
		}
		return okClient.open(url);
	}
}
