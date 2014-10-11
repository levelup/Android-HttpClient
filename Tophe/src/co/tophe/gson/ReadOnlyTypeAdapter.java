package co.tophe.gson;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Created by robUx4 on 22/08/2014.
 */
public abstract class ReadOnlyTypeAdapter<T> extends TypeAdapter<T> {
	public static final Gson DEFAULT_GSON = new Gson();

	public static <T> T fromJson(JsonReader in, Class<T> clazz) {
		return fromJson(DEFAULT_GSON, in, clazz);
	}

	public static <T> T fromJson(Gson gson, JsonReader in, Class<T> clazz) {
		return (T) gson.fromJson(in, clazz);
	}

	@Override
	public final void write(JsonWriter out, T value) throws IOException {
		throw new IllegalAccessError("not implemented");
	}
}
