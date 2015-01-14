package co.tophe;


import android.text.format.DateUtils;

/**
 * Basic HTTP configuration to access servers.
 * <p>For GET request the read timeout is 6s, for post the read timeout is 80s.</p>
 *
 * @see co.tophe.HttpConfig
 */
public class BasicHttpConfig implements HttpConfig {

	public static final int READ_TIMEOUT_IN_MS = (int) (6 * DateUtils.SECOND_IN_MILLIS);
	public static final int READ_TIMEOUT_LONG_POST_IN_MS = (int) (80 * DateUtils.SECOND_IN_MILLIS);

	public static final BasicHttpConfig INSTANCE = new BasicHttpConfig();

	public BasicHttpConfig() {
	}

	@Override
	public int getReadTimeout(HttpRequestInfo request) {
		if (null!=request) {
			final String postType = request.getHeader("Content-Type");
			if (null!=postType && postType.startsWith("multipart/form-data"))
				return READ_TIMEOUT_LONG_POST_IN_MS;
		}

		return READ_TIMEOUT_IN_MS;
	}
}
