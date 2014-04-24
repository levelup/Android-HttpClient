package com.levelup.http.gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import com.google.gson.Gson;
import com.levelup.http.HttpRequest;
import com.levelup.http.InputStreamParser;
import com.levelup.http.Util;

public class InputStreamGsonParser<T> implements InputStreamParser<T> {

	private final Gson gson;
	private final Type type;
	private final String charset;

	public InputStreamGsonParser(Gson gson, Type type) {
		this(gson, type, "UTF-8");
	}

	public InputStreamGsonParser(Gson gson, Type type, String charset) {
		this.gson = gson;
		this.type = type;
		this.charset = charset;
	}

	@Override
	public T parseInputStream(InputStream inputStream, HttpRequest request) throws IOException {
		final Charset readCharset;
		if ("UTF-8".equals(charset))
			readCharset = Util.getInputCharsetOrUtf8(request);
		else
			readCharset = Util.getInputCharset(request, Charset.forName(charset));
		
		InputStreamReader reader = new InputStreamReader(inputStream, readCharset);
		try {
			return gson.fromJson(reader, type);
		} finally {
			reader.close();
		}
	}

}
