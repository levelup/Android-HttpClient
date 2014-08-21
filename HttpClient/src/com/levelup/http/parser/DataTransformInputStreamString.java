package com.levelup.http.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.levelup.http.DataErrorException;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;
import com.levelup.http.Util;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class DataTransformInputStreamString implements DataTransform<InputStream,String> {
	public static final DataTransformInputStreamString INSTANCE = new DataTransformInputStreamString();

	private DataTransformInputStreamString() {
	}

	@Override
	public String transform(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException {
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
				reader = new BufferedReader(new InputStreamReader(inputStream, Util.getInputCharsetOrUtf8(request.getHttpResponse())), 1250);
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
