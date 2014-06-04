package com.levelup.http;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Factory that turns a HTTP {@link HttpRequest} into a {@link HttpURLConnection} 
 */
public interface HttpUrlConnectionFactory {
	HttpURLConnection createConnection(HttpRequest request) throws IOException;
}
