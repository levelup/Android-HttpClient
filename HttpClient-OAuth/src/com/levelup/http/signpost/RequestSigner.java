package com.levelup.http.signpost;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import oauth.signpost.AbstractOAuthConsumer;
import oauth.signpost.basic.HttpURLConnectionRequestAdapter;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.http.HttpParameters;

import org.apache.http.protocol.HTTP;

import android.text.TextUtils;

import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;
import com.levelup.http.HttpRequestPost;

/**
 * Helper class to OAuth sign a {@link HttpRequest} using <a href="https://code.google.com/p/oauth-signpost/">oauth-signpost</a>
 */
public class RequestSigner {

	private final OAuthClientApp clientApp;
	private final OAuthUser user;
	private OAuthConsumer mOAuthConsumer;

	public RequestSigner(OAuthClientApp clientApp, OAuthUser user) {
		this.clientApp = clientApp;
		this.user = user;
	}

	public synchronized void sign(HttpRequest req, HttpURLConnection conn, HttpParameters oauthParams) {
		if (mOAuthConsumer==null) {
			mOAuthConsumer = new OAuthConsumer(clientApp);
		}
		mOAuthConsumer.setTokenWithSecret(user.getToken(), user.getTokenSecret());
		mOAuthConsumer.setAdditionalParameters(oauthParams);
		
		try {
			mOAuthConsumer.sign(new RequestConnection(req, conn));
		} catch (OAuthException e) {
			HttpException.Builder builder = req.newException();
			builder.setErrorCode(HttpException.ERROR_AUTH);
			builder.setErrorMessage("Bad OAuth for "+user+" on "+req);
			builder.setCause(e);
			throw builder.build();
		}

	}

	private static class RequestConnection {
		private final HttpRequest req;
		private final HttpURLConnection conn;

		RequestConnection(HttpRequest req, HttpURLConnection conn) {
			this.req = req;
			this.conn = conn;
		}
	}

	private static class OAuthRequestAdapter extends HttpURLConnectionRequestAdapter {
		private final HttpRequest req;

		OAuthRequestAdapter(RequestConnection reqConn) {
			super(reqConn.conn);
			this.req = reqConn.req;
		}

		@Override
		public InputStream getMessagePayload() throws IOException {
			final String contentType = getContentType();  
			if (null != contentType && contentType.startsWith("application/x-www-form-urlencoded")) {
				String contentLength = connection.getRequestProperty(HTTP.CONTENT_LEN);
				ByteArrayOutputStream output = new ByteArrayOutputStream(TextUtils.isEmpty(contentLength) ? 32 : Integer.parseInt(contentLength));
				((HttpRequestPost) req).outputBody(output);
				return new ByteArrayInputStream(output.toByteArray());
			}
			return super.getMessagePayload();
		}

	}

	private static class OAuthConsumer extends AbstractOAuthConsumer {

		private static final long serialVersionUID = 8890615728426576510L;

		OAuthConsumer(OAuthClientApp clientApp) {
			super(clientApp.getConsumerKey(), clientApp.getConsumerSecret());
		}

		@Override
		protected oauth.signpost.http.HttpRequest wrap(Object request) {
			return new OAuthRequestAdapter((RequestConnection) request);
		}
	}
}
