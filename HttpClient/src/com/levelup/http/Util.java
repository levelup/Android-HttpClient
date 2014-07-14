package com.levelup.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;

public final class Util {

	private Util() {
	}

	public final static MediaType MediaTypeJSON = MediaType.parse("application/json");
	
	/**
	 * Get the {@link Charset} of the HTTP response of the provided httpResponse or UTF-8
	 * @param httpResponse received the server
	 * @return The Charset of specified by the server, if found locally, or UTF-8
	 * @throws IOException
	 */
	@SuppressLint("NewApi")
	public static Charset getInputCharsetOrUtf8(HttpResponse httpResponse) throws IOException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			return getInputCharset(httpResponse, StandardCharsets.UTF_8);
		
		return getInputCharset(httpResponse, Charset.forName("UTF-8"));
	}
	
	/**
	 * Get the {@link Charset} of the HTTP response of the provided httpResponse
	 * @param httpResponse that was queried on the server
	 * @param defaultCharset to use if the server sets no Charset or it's not found locally
	 * @return The Charset of specified by the server, if found locally, otherwise {@code defaultCharset}
	 */
	public static Charset getInputCharset(HttpResponse httpResponse, Charset defaultCharset) {
		Charset readCharset = defaultCharset;
		String contentType = httpResponse.getContentType();
		if (!TextUtils.isEmpty(contentType)) {
			MediaType type = MediaType.parse(contentType);
			if (null!=type && null!=type.charset())
				readCharset = type.charset();
		}
		return readCharset;
	}

}
