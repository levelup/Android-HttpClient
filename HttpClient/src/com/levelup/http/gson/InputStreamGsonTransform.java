package com.levelup.http.gson;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.levelup.http.GsonStreamParser;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.InputStreamParser;
import com.levelup.http.ParserException;

/**
 * Parse the network data using Gson and then transform them from type {@link G} to {@link T}
 * <p/>
 * Created by robUx4 on 8/1/2014.
 */
public abstract class InputStreamGsonTransform<T, G> implements InputStreamParser<T>, GsonStreamParser<T> {
	private static Gson defaultGsonParser = new GsonBuilder().create();

	private boolean debugData;

	final Gson gson;
	final Type type;
	final TypeToken typeToken;

	public InputStreamGsonTransform(Type type) {
		this(defaultGsonParser, type);
	}

	public InputStreamGsonTransform(Gson gson, Type type) {
		this(gson, type, null);
	}

	public InputStreamGsonTransform(TypeToken<G> typeToken) {
		this(defaultGsonParser, typeToken);
	}

	public InputStreamGsonTransform(Gson gson, TypeToken<G> typeToken) {
		this(gson, typeToken.getType(), typeToken);
	}

	private InputStreamGsonTransform(Gson gson, Type type, TypeToken<G> typeToken) {
		this.gson = gson;
		this.type = type;
		this.typeToken = typeToken;
	}

	public InputStreamGsonTransform(final InputStreamGsonParser<G> gsonParser) {
		this(gsonParser.gson, gsonParser.type, gsonParser.typeToken);
	}

	/**
	 * Enable debugging of bogus data by providing the data in the {@link com.levelup.http.ParserException} message
	 * @param enable
	 */
	public InputStreamGsonTransform<T,G> enableDebugData(boolean enable) {
		debugData = enable;
		return this;
	}

	public GsonStreamParser<T> getGsonParser() {
		return this;
	}

	protected abstract T transformGsonResult(G gsonResult);

	@Override
	public T parseInputStream(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException {
		throw new IllegalAccessError("parse the Gson internally");
	}

	@Override
	public Gson getGsonHandler() {
		return gson;
	}

	@Override
	public Type getGsonOutputType() {
		return type;
	}

	@Override
	public TypeToken getGsonOutputTypeToken() {
		return typeToken;
	}

	@Override
	public final T transformResult(Object gsonResult) {
		return transformGsonResult((G) gsonResult);
	}
}
