package com.levelup.http.okhttp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HashSet;

import javax.net.ssl.SSLContext;

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

	private final HashSet<String> domainBlackList = new HashSet<String>(); 

	static {
		instance = new OkHttpClient();
		if (__WITH_OKHTTP) {
			okClient = new com.squareup.okhttp.OkHttpClient();
			SSLContext sslContext;
			try {
				sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, null, new SecureRandom());
			} catch (GeneralSecurityException e) {
				throw new AssertionError(); // The system has no TLS. Just give up.
			}
			okClient.setSslSocketFactory(sslContext.getSocketFactory());

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
	 * Add a domain that should not use OkHttp
	 * <p>subdomains and wildcards not supported</p> 
	 */
	public static void addUrlBlacklist(String domain) {
		synchronized (instance.domainBlackList) {
			instance.domainBlackList.add(domain);
		}
	}

	/**
	 * Allow a domain to use OkHttp again
	 * <p>subdomains and wildcards not supported</p> 
	 */
	public static void removeUrlBlacklist(String domain) {
		synchronized (instance.domainBlackList) {
			instance.domainBlackList.remove(domain);
		}
	}

	@Override
	public HttpURLConnection createConnection(URL url) throws IOException {
		if (null == okClient)
			return (HttpURLConnection) url.openConnection();

		synchronized (domainBlackList) {
			String urlString = url.toExternalForm();
			for (String domain : domainBlackList) {
				if (urlString.contains(domain)) {
					return (HttpURLConnection) url.openConnection();
				}
			}
			return okClient.open(url);
		}
	}
}
