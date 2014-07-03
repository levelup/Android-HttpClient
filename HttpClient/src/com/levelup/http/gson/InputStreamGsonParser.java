package com.levelup.http.gson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.levelup.http.HttpRequest;
import com.levelup.http.InputStreamParser;
import com.levelup.http.InputStreamStringParser;
import com.levelup.http.ParserException;
import com.levelup.http.Util;

public class InputStreamGsonParser<T> implements InputStreamParser<T> {

	public final Gson gson;
	public final Type type;
	public final TypeToken<T> typeToken;
	private final String charset;
	private boolean debugData;

	public InputStreamGsonParser(Gson gson, Type  type) {
		this(gson, type, "UTF-8");
	}

	public InputStreamGsonParser(Gson gson, Type type, String charset) {
		this.gson = gson;
		this.charset = charset;
		this.type = type;
		this.typeToken = null;
	}

	public InputStreamGsonParser(Gson gson, TypeToken<T> typeToken) {
		this(gson, typeToken, "UTF-8");
	}

	public InputStreamGsonParser(Gson gson, TypeToken<T> typeToken, String charset) {
		this.gson = gson;
		this.charset = charset;
		this.typeToken = typeToken;
		this.type = typeToken.getType();
	}

	/**
	 * Enable debugging of bogus data by providing the data in the {@link com.levelup.http.ParserException} message
	 * @param enable
	 */
	public InputStreamGsonParser<T> enableDebugData(boolean enable) {
		debugData = enable;
		return this;
	}

	@Override
	public T parseInputStream(InputStream inputStream, HttpRequest request) throws IOException, ParserException {
		final Charset readCharset;
		if ("UTF-8".equals(charset))
			readCharset = Util.getInputCharsetOrUtf8(request);
		else
			readCharset = Util.getInputCharset(request, Charset.forName(charset));

		String fullData = null;
		if (debugData) {
			fullData = InputStreamStringParser.instance.parseInputStream(inputStream, request);
			inputStream = new ByteArrayInputStream(fullData.getBytes());
		}

		Reader reader = new InputStreamReader(inputStream, readCharset);
		try {
			return gson.fromJson(reader, type);
		} catch (JsonIOException e) {
			throw (IOException) new IOException().initCause(e);
		} catch (JsonSyntaxException e) {
			throw new ParserException("Bad Json data", e, fullData);
		} finally {
			reader.close();
		}
	}

}
