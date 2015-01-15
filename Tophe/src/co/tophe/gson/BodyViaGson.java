package co.tophe.gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import co.tophe.parser.BodyTransformChain;

/**
 * A {@link co.tophe.parser.XferTransform} class that turns an {@link co.tophe.HttpResponse} into type {@link T} using Gson.
 *
 * @param <T> Output type after Gson parsing
 * @author Created by Steve Lhomme on 26/06/2014.
 */
public class BodyViaGson<T> extends BodyTransformChain<T> {
	/**
	 * @deprecated Use {@link #asList(Class)} for List content or {@link #BodyViaGson(java.lang.reflect.Type)} for a class
	 */
	public BodyViaGson(TypeToken<T> typeToken) {
		super(new XferTransformViaGson<T>(typeToken));
	}

	/**
	 * Constructor to output to the specified type.
	 */
	public BodyViaGson(Type type) {
		super(new XferTransformViaGson<T>(type));
	}

	/**
	 * @deprecated Use {@link #asList(com.google.gson.Gson, Class)} for List content or {@link #BodyViaGson(com.google.gson.Gson, java.lang.reflect.Type)} for a class
	 */
	public BodyViaGson(Gson gson, TypeToken<T> typeToken) {
		super(new XferTransformViaGson<T>(gson, typeToken));
	}

	/**
	 * Constructor to output to the specified type with a custom {@link com.google.gson.Gson} handler.
	 * @param gson a {@link com.google.gson.Gson} object where you can specify custom class parsing.
	 * @param type the output type.
	 */
	public BodyViaGson(Gson gson, Type type) {
		super(new XferTransformViaGson<T>(gson, type));
	}

	/**
	 * Enable debugging of bogus data by providing the raw JSON data in the {@link co.tophe.parser.ParserException} when it's raised.
	 */
	public BodyViaGson<T> enableDebugData(boolean enable) {
		((XferTransformViaGson) transforms[1]).enableDebugData(enable);
		return this;
	}

	/**
	 * Create a {@link co.tophe.gson.BodyViaGson} to read a list of items of the specified class.
	 * @param <T> type of the elements in the List.
	 */
	public static <T> BodyViaGson<List<T>> asList(Class<T> clazz) {
		// TODO use a weakreference cache
		Type type = new ListParameterizedType(clazz);
		return new BodyViaGson<List<T>>(type);
	}

	/**
	 * Create a {@link co.tophe.gson.BodyViaGson} to read a list of items of the specified class with a custom {@link com.google.gson.Gson} handler.
	 * @param gson a {@link com.google.gson.Gson} object where you can specify custom class parsing.
	 * @param <T> type of the elements in the List.
	 */
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
			if (o == this) return true;
			if (!(o instanceof ListParameterizedType)) return false;
			return type.equals(((ListParameterizedType) o).type);
		}
	}
}
