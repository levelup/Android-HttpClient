package com.levelup.http;

/**
 * An {@link com.levelup.http.InputStreamParser} that doesn't handle anything using Gson
 *
 * Created by robUx4 on 8/1/2014.
 */
public abstract class BaseInputStreamParser<T> implements InputStreamParser<T> {
	@Override
	public GsonStreamParser<T> getGsonParser() {
		return null;
	}
}
