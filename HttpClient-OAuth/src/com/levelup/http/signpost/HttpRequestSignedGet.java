package com.levelup.http.signpost;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

import android.net.Uri;

import com.levelup.http.HttpGetParameters;
import com.levelup.http.HttpRequestGet;

public class HttpRequestSignedGet extends HttpRequestGet {

	private final RequestSigner signer;

	public HttpRequestSignedGet(RequestSigner signer, String baseUrl, HttpGetParameters httpParams) {
		super(baseUrl, httpParams);
		this.signer = signer;
	}

	public HttpRequestSignedGet(RequestSigner signer, Uri baseUri, HttpGetParameters httpParams) {
		super(baseUri, httpParams);
		this.signer = signer;
	}

	public HttpRequestSignedGet(RequestSigner signer, String url) {
		super(url);
		this.signer = signer;
	}

	@Override
	public void setRequestProperties(HttpURLConnection connection) throws ProtocolException {
		super.setRequestProperties(connection);

		signer.sign(this, connection, null);
	}
}
