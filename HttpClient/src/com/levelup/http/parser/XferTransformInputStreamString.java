package com.levelup.http.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.levelup.http.CharsetUtils;
import com.levelup.http.ImmutableHttpRequest;

/**
 * <p>A {@link com.levelup.http.parser.XferTransform} to turn an {@code InputStream} into a {@code String}, using the charset from the HTTP reply</p>
 *
 * <p>Use the {@link #INSTANCE}</p>
 *
 * @see BodyToString
 * @author Created by robUx4 on 20/08/2014.
 */
public final class XferTransformInputStreamString implements XferTransform<InputStream,String> {
	public static final XferTransformInputStreamString INSTANCE = new XferTransformInputStreamString();

	private XferTransformInputStreamString() {
	}

	@Override
	public String transformData(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException {
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
				reader = new BufferedReader(new InputStreamReader(inputStream, CharsetUtils.getInputCharsetOrUtf8(request.getHttpResponse())), 1250);
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

		return sb.toString();
	}
}
