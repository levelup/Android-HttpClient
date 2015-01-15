package co.tophe.gson;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Helper class for Gson {@link com.google.gson.TypeAdapter} that doesn't support writing the Type into JSON.
 *
 * @param <T> the type of the object returned by Gson when reading the JSON stream.
 * @author Created by robUx4 on 22/08/2014.
 */
public abstract class ReadOnlyTypeAdapter<T> extends TypeAdapter<T> {

	public static <T> T fromJson(JsonReader in, Class<T> clazz) {
		return fromJson(XferTransformViaGson.DEFAULT_GSON_PARSER, in, clazz);
	}

	public static <T> T fromJson(Gson gson, JsonReader in, Class<T> clazz) {
		return (T) gson.fromJson(in, clazz);
	}

	@Override
	public final void write(JsonWriter out, T value) throws IOException {
		throw new IllegalAccessError("not implemented");
	}
}
