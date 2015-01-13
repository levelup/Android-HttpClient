package co.tophe.signed.oauth1;

import java.io.InputStream;
import java.util.Date;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import co.tophe.BaseResponseHandler;
import co.tophe.HttpRequest;
import co.tophe.HttpResponse;
import co.tophe.log.LogManager;
import co.tophe.parser.XferTransformResponseInputStream;
import co.tophe.signed.OAuthClientApp;


/**
 * An {@link HttpClientOAuth1Consumer OAuth Consumer} that can handle a device clock shift transparently
 */
public class OAuth1ConsumerClocked extends HttpClientOAuth1Consumer {

	private static final long serialVersionUID = 3963386898609696262L;

	private long serverDelayInMilliseconds;

	final BaseResponseHandler<InputStream> responseHandler = new BaseResponseHandler<InputStream>(XferTransformResponseInputStream.INSTANCE) {
		@Override
		public void onHttpResponse(@NonNull HttpRequest request, @NonNull HttpResponse response) {
			super.onHttpResponse(request, response);

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
			Date serverDate = DateUtils.parseDate(value);
			serverDelayInMilliseconds = now - serverDate.getTime();
		} catch (DateParseException ignored) {
			LogManager.getLogger().d("invalid OAuth1 server date:"+value);
		}
	}

	public OAuth1ConsumerClocked(OAuthClientApp clientApp) {
		super(clientApp);
	}
}