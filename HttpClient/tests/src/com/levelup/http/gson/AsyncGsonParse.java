package com.levelup.http.gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.test.AndroidTestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpRequestGet;

public class AsyncGsonParse extends AndroidTestCase {

	private static final String JSON_URL = "http://social.appxoid.com/json/get_apps_by_pages2";
	private static final String JSON_DATE_FORMAT = "yyyy-mm-dd'T'HH:mm:ss";

	public static class AppXoidInfo {
		@SerializedName("available_fr") boolean availableInFrance;
		@SerializedName("rating") String rating;
		@SerializedName("app_updated") Date dateUpdated;
		@SerializedName("creator") String creator;
	}

	public static class AppXoid {
		@SerializedName("pk") int apkNum;
		@SerializedName("model") String model;
		@SerializedName("fields") AppXoidInfo data;
	}

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		HttpClient.setup(context);
	}

	public void testGsonParser() throws Exception {
		Gson gson = new GsonBuilder().setDateFormat(JSON_DATE_FORMAT).create();

		BodyViaGson<List<AppXoid>> parser = new BodyViaGson<List<AppXoid>>(gson, new TypeToken<List<AppXoid>>(){});
		HttpRequestGet<List<AppXoid>> request = new HttpRequestGet<List<AppXoid>>(JSON_URL, parser);
		List<AppXoid> items = HttpClient.parseRequest(request);
		assertNotNull(items);
	}

	public static class AppXoidReader {
		@SerializedName("pk") int apkNum;
		@SerializedName("model") String model;
		@SerializedName("fields") AppXoidInfo data;
	}

	public static class AppXoidInfoReader {
		public final boolean availableInFrance;
		public final String rating;
		public final Date dateUpdated;
		public final String creator;

		private AppXoidInfoReader(Builder builder) {
			this.availableInFrance = builder.availableInFrance;
			this.rating = builder.rating;
			this.dateUpdated = builder.dateUpdated;
			this.creator = builder.creator;
		}

		public static final class Builder {
			public boolean availableInFrance;
			public String rating;
			public Date dateUpdated;
			public String creator;

			public AppXoidInfoReader build() {
				return new AppXoidInfoReader(this);
			}
		}
	}

	public void testGsonDeserialize() throws Exception {
		TypeAdapter<AppXoidInfoReader> infoAdapter = new ReadOnlyTypeAdapter<AppXoidInfoReader>() {
			private final SimpleDateFormat format = new SimpleDateFormat(JSON_DATE_FORMAT);
			@Override
			public AppXoidInfoReader read(JsonReader reader) throws IOException {
				// the first token is the start object
				JsonToken token = reader.peek();
				AppXoidInfoReader.Builder builder = new AppXoidInfoReader.Builder();
				if (token.equals(JsonToken.BEGIN_OBJECT)) {
					reader.beginObject();
					while (!reader.peek().equals(JsonToken.END_OBJECT)) {
						if (reader.peek().equals(JsonToken.NAME)) {
							String name = reader.nextName();
							if ("available_fr".equals(name))
								builder.availableInFrance = reader.nextBoolean();
							else if ("rating".equals(name))
								builder.rating = reader.nextString();
							else if ("app_updated".equals(name))
								try {
									builder.dateUpdated = format.parse(reader.nextString());
								} catch (ParseException e) {
								}
							else if ("creator".equals(name))
								builder.creator = reader.nextString();
							else
								reader.skipValue();
						}
					}
					reader.endObject();
				}
				return builder.build();
			}
		};

		Gson gson = new GsonBuilder()
		.registerTypeAdapter(AppXoidInfoReader.class, infoAdapter)
		.setDateFormat(JSON_DATE_FORMAT)
		.create();

		BodyViaGson<List<AppXoidReader>> parser = new BodyViaGson<List<AppXoidReader>>(gson, new TypeToken<List<AppXoidReader>>(){});
		HttpRequestGet<List<AppXoidReader>> request = new HttpRequestGet<List<AppXoidReader>>(JSON_URL, parser);
		List<AppXoidReader> items = HttpClient.parseRequest(request);
		assertNotNull(items);
	}
}
