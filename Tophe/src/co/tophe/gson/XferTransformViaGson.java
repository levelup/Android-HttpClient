package co.tophe.gson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import co.tophe.CharsetUtils;
import co.tophe.ImmutableHttpRequest;
import co.tophe.parser.ParserException;
import co.tophe.parser.XferTransform;
import co.tophe.parser.XferTransformInputStreamString;

/**
 * Parse the HTTP response body into type {@code T} using Gson.
 *
 * @param <T> Type of the object returned by the Gson processing.
 * @author Created by robUx4 on 8/1/2014.
 * @see co.tophe.gson.BodyViaGson
 */
public class XferTransformViaGson<T> implements XferTransform<InputStream, T> {
	public static final Gson DEFAULT_GSON_PARSER = new GsonBuilder().create();

	private boolean debugData;

	@NonNull
	final Gson gson;
	@NonNull
	final Type type;
	@Nullable
	final TypeToken typeToken;

	/**
	 * Constructor to transform into the specified type.
	 *
	 * @param type
	 * @see co.tophe.gson.BodyViaGson#BodyViaGson(java.lang.reflect.Type)
	 */
	public XferTransformViaGson(@NonNull Type type) {
		this(DEFAULT_GSON_PARSER, type);
	}

	/**
	 * Constructor to transform into the specified type with a custom {@link com.google.gson.Gson} handler.
	 *
	 * @param gson
	 * @param type
	 * @see co.tophe.gson.BodyViaGson#BodyViaGson(com.google.gson.Gson, java.lang.reflect.Type)
	 */
	public XferTransformViaGson(@NonNull Gson gson, @NonNull Type type) {
		this(gson, type, null);
	}

	/**
	 * Constructor to transform into the specified list type.
	 *
	 * @param typeToken
	 * @see co.tophe.gson.BodyViaGson#asList(Class)
	 */
	public XferTransformViaGson(@NonNull TypeToken<T> typeToken) {
		this(DEFAULT_GSON_PARSER, typeToken);
	}

	/**
	 * Constructor to transform into the specified list type with a custom {@link com.google.gson.Gson} handler.
	 *
	 * @param gson
	 * @param typeToken
	 * @see co.tophe.gson.BodyViaGson#asList(com.google.gson.Gson, Class)
	 */
	public XferTransformViaGson(@NonNull Gson gson, @NonNull TypeToken<T> typeToken) {
		this(gson, typeToken.getType(), typeToken);
	}

	private XferTransformViaGson(@NonNull Gson gson, @NonNull Type type, @Nullable TypeToken<T> typeToken) {
		this.gson = gson;
		this.type = type;
		this.typeToken = typeToken;
	}

	/**
	 * Enable debugging of bogus data by providing the raw JSON data in the {@link co.tophe.parser.ParserException} when it's raised.
	 *
	 * @param enable
	 */
	public XferTransformViaGson<T> enableDebugData(boolean enable) {
		debugData = enable;
		return this;
	}

	public boolean debugEnabled() {
		return debugData;
	}

	@Override
	public T transformData(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException {
		String dataString = null;
		if (debugData) {
			dataString = XferTransformInputStreamString.INSTANCE.transformData(inputStream, request);
			inputStream = new ByteArrayInputStream(dataString.getBytes());
		}

		Charset readCharset = CharsetUtils.getInputCharsetOrUtf8(request.getHttpResponse());
		InputStreamReader ir = new InputStreamReader(inputStream, readCharset);
		try {
			JsonReader reader = new JsonReader(ir);
			return gson.fromJson(reader, type);
		} catch (JsonParseException e) {
			if (e.getCause() instanceof IOException)
				throw (IOException) e.getCause();

			throw new ParserException("Bad data for GSON", e, dataString);
		} finally {
			try {
				ir.close();
			} catch (IOException ignored) {
			}
		}
	}

	/**
	 * Get the {@link com.google.gson.Gson} instance used to process the received data.
	 */
	@NonNull
	public Gson getGsonHandler() {
		return gson;
	}

	/**
	 * Get the output {@link java.lang.reflect.Type} from the Gson processing.
	 */
	@NonNull
	public Type getGsonOutputType() {
		return type;
	}

	/**
	 * Get the output {@link com.google.gson.reflect.TypeToken} from the Gson processing. May be {@code null}.
	 */
	@Nullable
	public TypeToken getGsonOutputTypeToken() {
		return typeToken;
	}

	@Override
	public boolean equals(Object o) {
		if (o==this) return true;
		if (!(o instanceof XferTransformViaGson)) return false;
		XferTransformViaGson og = (XferTransformViaGson) o;
		return og.type.equals(type) && og.gson.equals(gson);
	}
}
