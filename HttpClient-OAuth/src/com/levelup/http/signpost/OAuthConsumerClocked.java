package com.levelup.http.signpost;

import java.net.HttpURLConnection;
import java.util.Date;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import android.text.TextUtils;

import com.levelup.http.HttpRequest;
import com.levelup.http.HttpRequestPost;


/**
 * An {@link HttpClientOAuthConsumer OAuth Consumer} that can handle a device clock shift transparently
 */
public class OAuthConsumerClocked extends HttpClientOAuthConsumer {

	private static final long serialVersionUID = 3963386898609696262L;

	private long serverDelayInMilliseconds;

	public long getServerTime() {
		return System.currentTimeMillis() - serverDelayInMilliseconds;
	}

	@Override
	protected String generateTimestamp() {
		return String.valueOf(getServerTime() / 1000);
	}

	public void setServerDate(String value) {
		long now = System.currentTimeMillis();
		try {
			//Date twitterDate = getDate(DATE_FORMAT_HTTP, value);
			Date twitterDate = DateUtils.parseDate(value);
			serverDelayInMilliseconds = now - twitterDate.getTime();
		} catch (DateParseException e) {
		}
	}

	public OAuthConsumerClocked(OAuthClientApp clientApp) {
		super(clientApp);
	}

	public final ProviderHttpRequestFactory providerRequestFactory = new ProviderHttpRequestFactory() {
		@Override
		public HttpRequest createRequest(String endpointUrl) {
			return new HttpRequestPost(endpointUrl, null) {
				@Override
				public void setResponse(HttpURLConnection resp) {
					super.setResponse(resp);

					if (null!=resp) {
						String serverDate = resp.getHeaderField("date");
						if (!TextUtils.isEmpty(serverDate)) {
							setServerDate(serverDate);
						}
					}
				}
			};
		}
	};
}