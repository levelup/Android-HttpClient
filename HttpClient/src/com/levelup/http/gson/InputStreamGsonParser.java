package com.levelup.http.gson;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.levelup.http.GsonStreamParser;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.InputStreamParser;
import com.levelup.http.ParserException;

public class InputStreamGsonParser<T> extends InputStreamGsonTransform<T,T> {

	public InputStreamGsonParser(final Gson gson, final Type type) {
		super(gson, type);
	}

	public InputStreamGsonParser(Gson gson, TypeToken<T> typeToken) {
		super(gson, typeToken.getType());
	}

	public InputStreamGsonParser(Type type) {
		super(type);
	}

	public InputStreamGsonParser(TypeToken<T> typeToken) {
		super(typeToken);
	}

	@Override
	public T transformGsonResult(Object gsonResult) {
		return (T) gsonResult;
	}

	@Deprecated
	@Override
	public T parseInputStream(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException {
		throw new IllegalAccessError("the HTTP engine should hook with Gson directly");
	}
}
