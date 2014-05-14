package com.levelup.http;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

import android.net.Uri;

/**
 * An Http GET request that doesn't follow redirections
 *  
 * @author Steve Lhomme
 */
public class HttpRequestGetNoRedirect extends HttpRequestGet {

	public HttpRequestGetNoRedirect(String baseUrl) {
		super(baseUrl);
	}

	public HttpRequestGetNoRedirect(String baseUrl, HttpGetParameters httpParams) {
		super(baseUrl, httpParams);
	}

	public HttpRequestGetNoRedirect(Uri baseUrl, HttpGetParameters httpParams) {
		super(baseUrl, httpParams);
	}

	@Override
	public void setConnectionProperties(HttpURLConnection connection) throws ProtocolException {
		super.setConnectionProperties(connection);
		connection.setInstanceFollowRedirects(false);
	}
}