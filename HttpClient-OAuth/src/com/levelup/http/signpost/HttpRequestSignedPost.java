package com.levelup.http.signpost;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

import android.net.Uri;

import com.levelup.http.HttpPostParameters;
import com.levelup.http.HttpRequestPost;

public class HttpRequestSignedPost extends HttpRequestPost {

	private final RequestSigner signer;

	public HttpRequestSignedPost(RequestSigner signer, String url, HttpPostParameters httpParams) {
		super(url, httpParams);
		this.signer = signer;
	}

	public HttpRequestSignedPost(RequestSigner signer, Uri uri, HttpPostParameters httpParams) {
		super(uri, httpParams);
		this.signer = signer;
	}


	@Override
	public void setRequestProperties(HttpURLConnection connection) throws ProtocolException {
		super.setRequestProperties(connection);

		signer.sign(this, connection, null);
	}
}
