package com.levelup.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Factory that turns a HTTP {@link URL} into a {@link HttpURLConnection} 
 */
public interface HttpUrlConnectionFactory {
	HttpURLConnection createConnection(URL url) throws IOException;
}
