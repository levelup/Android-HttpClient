package com.levelup.http.signed.oauth1;

import java.util.Date;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import android.text.TextUtils;

import com.levelup.http.HttpResponse;
import com.levelup.http.RawHttpRequest;
import com.levelup.http.signed.OAuthClientApp;


/**
 * An {@link HttpClientOAuth1Consumer OAuth Consumer} that can handle a device clock shift transparently
 */
public class OAuth1ConsumerClocked extends HttpClientOAuth1Consumer {

	private static final long serialVersionUID = 3963386898609696262L;

	private long serverDelayInMilliseconds;

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

	private class HttpProviderRequest extends RawHttpRequest {
		protected HttpProviderRequest(String endpointUrl) {
			super(new RawHttpRequest.Builder().setUrl(endpointUrl).setHttpMethod("POST"));
		}

		@Override
		public void setResponse(HttpResponse resp) {
			super.setResponse(resp);

			if (null!=resp) {
				String serverDate = resp.getHeaderField("Date");
				if (!TextUtils.isEmpty(serverDate)) {
					setServerDate(serverDate);
				}
			}
		}
	}

	public RawHttpRequest createRequest(String endpointUrl) {
		return new HttpProviderRequest(endpointUrl);
	}
}