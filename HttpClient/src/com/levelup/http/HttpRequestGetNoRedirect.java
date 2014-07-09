package com.levelup.http;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

import android.net.Uri;

/**
 * An Http GET request that doesn't follow redirections
 *
 * @author Steve Lhomme
 */
public class HttpRequestGetNoRedirect extends HttpRequestGet<Void> {

	public HttpRequestGetNoRedirect(String baseUrl) {
		super(baseUrl, null);
	}

	public HttpRequestGetNoRedirect(String baseUrl, HttpUriParameters uriParams) {
		super(baseUrl, uriParams);
	}

	public HttpRequestGetNoRedirect(Uri baseUrl, HttpUriParameters uriParams) {
		super(baseUrl, uriParams);
	}
}