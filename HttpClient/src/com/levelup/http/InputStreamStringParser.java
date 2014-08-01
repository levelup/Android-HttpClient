package com.levelup.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Singleton {@link InputStreamParser} class to get a {@link String} from an {@link InputStream}
 * @see #instance
 */
public class InputStreamStringParser extends BaseInputStreamParser<String> {

	public static final InputStreamStringParser instance = new InputStreamStringParser();

	private InputStreamStringParser() {
	}

	@Override
	public String parseInputStream(InputStream is, ImmutableHttpRequest request) throws IOException {
		final StringBuilder sb;

		int contentLength = -1;
		if (null != request && request.getHttpResponse()!=null) {
			contentLength = request.getHttpResponse().getContentLength();
			if (contentLength > 0) {
				sb = new StringBuilder(contentLength);
			} else {
				sb = new StringBuilder();
			}
		} else {
			sb = new StringBuilder();
		}

		if (contentLength != 0) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(is, Util.getInputCharsetOrUtf8(request.getHttpResponse())), 1250);
				for (String line = reader.readLine(); line!=null; line = reader.readLine()) {
					if (sb.length()>0)
						sb.append('\n');
					sb.append(line);
				}
			} finally {
				if (null!=reader)
					reader.close();
			}
		}

		//if (null != httpResponse && null != httpResponse.getLogger()) {
		//	httpResponse.getLogger().d(httpResponse.toString() + '>' + sb.toString());
		//}

		return sb.toString();
	}
}
