package com.levelup.http.signed.oauth1;

import java.io.InputStream;
import java.util.Date;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import android.text.TextUtils;

import com.levelup.http.HttpRequest;
import com.levelup.http.HttpResponse;
import com.levelup.http.ResponseHandler;
import com.levelup.http.parser.XferTransformResponseInputStream;
import com.levelup.http.signed.OAuthClientApp;


/**
 * An {@link HttpClientOAuth1Consumer OAuth Consumer} that can handle a device clock shift transparently
 */
public class OAuth1ConsumerClocked extends HttpClientOAuth1Consumer {

	private static final long serialVersionUID = 3963386898609696262L;

	private long serverDelayInMilliseconds;

	final ResponseHandler<InputStream> responseHandler = new ResponseHandler<InputStream>(XferTransformResponseInputStream.INSTANCE) {
		@Override
		public void onNewResponse(HttpResponse response, HttpRequest request) {
			super.onNewResponse(response, request);

			String serverDate = response.getHeaderField("Date");
			if (!TextUtils.isEmpty(serverDate)) {
				setServerDate(serverDate);
			}
		}
	};

	public long getServerTime() {
		return System.currentTimeMillis() - serverDelayInMilliseconds;
	}

	@Override
	protected String generateTimestamp() {
		return String.valueOf(getServerTime() / 1000);
	}

	protected void setServerDate(String value) {
		long now = System.currentTimeMillis();
		try {
			//Date twitterDate = getDate(DATE_FORMAT_HTTP, value);
			Date twitterDate = DateUtils.parseDate(value);
			serverDelayInMilliseconds = now - twitterDate.getTime();
		} catch (DateParseException ignored) {
		}
	}

	public OAuth1ConsumerClocked(OAuthClientApp clientApp) {
		super(clientApp);
	}
}