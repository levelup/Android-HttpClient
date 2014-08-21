package com.levelup.http.gson;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.levelup.http.HttpResponse;
import com.levelup.http.parser.DataTransformChain;
import com.levelup.http.parser.DataTransformResponseInputStream;

/**
 * An {@link DataTransformGson} class that has debug enabled for alpha/beta builds
 *
 * Created by Steve Lhomme on 26/06/2014.
 */
public class ResponseToGson<T> extends DataTransformChain<HttpResponse,T> {
	public ResponseToGson(TypeToken<T> typeToken) {
		super(new Builder<HttpResponse, T>(DataTransformResponseInputStream.INSTANCE),
				new DataTransformGson<T>(typeToken));
	}

	public ResponseToGson(Type type) {
		super(new Builder<HttpResponse, T>(DataTransformResponseInputStream.INSTANCE),
				new DataTransformGson<T>(type));
	}

	public ResponseToGson(Gson gson, TypeToken<T> typeToken) {
		super(new Builder<HttpResponse, T>(DataTransformResponseInputStream.INSTANCE),
				new DataTransformGson<T>(gson, typeToken));
	}

	public ResponseToGson(Gson gson, Type type) {
		super(new Builder<HttpResponse, T>(DataTransformResponseInputStream.INSTANCE),
				new DataTransformGson<T>(gson, type));
	}

	public ResponseToGson<T> enableDebugData(boolean enable) {
		((DataTransformGson) transforms[1]).enableDebugData(enable);
		return this;
	}
}
