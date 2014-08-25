package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.parser.XferTransform;

/**
 * An {@link com.levelup.http.InputStreamParser} that doesn't handle anything using Gson
 *
 * Created by robUx4 on 8/1/2014.
 */
public abstract class BaseInputStreamParser<T> implements XferTransform<InputStream,T> {
	@Override
	public final T transform(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException {
		return parseInputStream(inputStream, request);
	}

	protected abstract T parseInputStream(InputStream is, ImmutableHttpRequest request) throws IOException, ParserException;

	/*
	@Override
	public GsonStreamParser<T> getGsonParser() {
		return null;
	}*/
}
