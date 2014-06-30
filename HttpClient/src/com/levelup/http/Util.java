package com.levelup.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;

public final class Util {

	private Util() {
	}

	public final static MediaType MediaTypeJSON = MediaType.parse("application/json");
	
	/**
	 * Get the {@link Charset} of the HTTP response of the provided request or UTF-8
	 * @param request that was queried on the server
	 * @return The Charset of specified by the server, if found locally, or UTF-8
	 * @throws IOException
	 */
	@SuppressLint("NewApi")
	public static Charset getInputCharsetOrUtf8(HttpRequest request) throws IOException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			return getInputCharset(request, StandardCharsets.UTF_8);
		
		return getInputCharset(request, Charset.forName("UTF-8"));
	}
	
	/**
	 * Get the {@link Charset} of the HTTP response of the provided request
	 * @param request that was queried on the server
	 * @param defaultCharset to use if the server sets no Charset or it's not found locally
	 * @return The Charset of specified by the server, if found locally, otherwise {@code defaultCharset}
	 */
	public static Charset getInputCharset(HttpRequest request, Charset defaultCharset) {
		Charset readCharset = defaultCharset;
		String contentType = request.getResponse().getHeaders().get(HTTP.CONTENT_TYPE);
		if (!TextUtils.isEmpty(contentType)) {
			MediaType type = MediaType.parse(contentType);
			if (null!=type && null!=type.charset())
				readCharset = type.charset();
		}
		return readCharset;
	}

}
