package com.levelup.http.gson;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.levelup.http.parser.ResponseTransformChain;

/**
 * An {@link XferTransformViaGson} class that has debug enabled for alpha/beta builds
 *
 * Created by Steve Lhomme on 26/06/2014.
 */
public class ResponseViaGson<T> extends ResponseTransformChain<T> {
	public ResponseViaGson(TypeToken<T> typeToken) {
		super(new Builder<T>(), new XferTransformViaGson<T>(typeToken));
	}

	public ResponseViaGson(Type type) {
		super(new Builder<T>(), new XferTransformViaGson<T>(type));
	}

	public ResponseViaGson(Gson gson, TypeToken<T> typeToken) {
		super(new Builder<T>(), new XferTransformViaGson<T>(gson, typeToken));
	}

	public ResponseViaGson(Gson gson, Type type) {
		super(new Builder<T>(), new XferTransformViaGson<T>(gson, type));
	}

	public ResponseViaGson<T> enableDebugData(boolean enable) {
		((XferTransformViaGson) transforms[1]).enableDebugData(enable);
		return this;
	}
}
