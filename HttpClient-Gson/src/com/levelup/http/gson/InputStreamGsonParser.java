package com.levelup.http.gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.levelup.http.HttpRequest;
import com.levelup.http.InputStreamParser;

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
		// TODO read the charset from the response header
		InputStreamReader reader = new InputStreamReader(inputStream, charset);
		try {
			return gson.fromJson(reader, type);
		} finally {
			reader.close();
		}
	}

}
