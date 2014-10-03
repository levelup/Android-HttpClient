package com.levelup.http.gson;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.levelup.http.parser.BodyTransformChain;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link XferTransformViaGson} class that has debug enabled for alpha/beta builds
 *
 * @author Created by Steve Lhomme on 26/06/2014.
 */
public class BodyViaGson<T> extends BodyTransformChain<T> {
	/**
	 * @deprecated Use {@link #asList(Class)} for List content or {@link #BodyViaGson(java.lang.reflect.Type)} for a class
	 */
	public BodyViaGson(TypeToken<T> typeToken) {
		super(new XferTransformViaGson<T>(typeToken));
	}

	public BodyViaGson(Type type) {
		super(new XferTransformViaGson<T>(type));
	}

	/**
	 * @deprecated Use {@link #asList(com.google.gson.Gson, Class)} for List content or {@link #BodyViaGson(com.google.gson.Gson, java.lang.reflect.Type)} for a class
	 */
	public BodyViaGson(Gson gson, TypeToken<T> typeToken) {
		super(new XferTransformViaGson<T>(gson, typeToken));
	}

	public BodyViaGson(Gson gson, Type type) {
		super(new XferTransformViaGson<T>(gson, type));
	}

	public BodyViaGson<T> enableDebugData(boolean enable) {
		((XferTransformViaGson) transforms[1]).enableDebugData(enable);
		return this;
	}

	public static <T> BodyViaGson<List<T>> asList(Class<T> clazz) {
		// TODO use a weakreference cache
		Type type = new ListParameterizedType(clazz);
		return new BodyViaGson<List<T>>(type);
	}

	public static <T> BodyViaGson<List<T>> asList(Gson gson, Class<T> clazz) {
		// TODO use a weakreference cache
		Type type = new ListParameterizedType(clazz);
		return new BodyViaGson<List<T>>(gson, type);
	}

	protected static class ListParameterizedType implements ParameterizedType {
		private final Type type;

		public ListParameterizedType(Type type) {
			this.type = type;
		}

		@Override
		public Type[] getActualTypeArguments() {
			return new Type[]{type};
		}

		@Override
		public Type getRawType() {
			return ArrayList.class;
		}

		@Override
		public Type getOwnerType() {
			return null;
		}

		@Override
		public boolean equals(Object o) {
			if (o==this) return true;
			if (!(o instanceof ListParameterizedType)) return false;
			return type.equals(((ListParameterizedType) o).type);
		}
	}
}
