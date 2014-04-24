package com.levelup.http.gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpRequestGet;
import com.levelup.http.gson.InputStreamGsonParser;

public class AsyncGsonParse extends TestCase {

	private static final String JSON_URL = "http://social.appxoid.com/json/get_apps_by_pages2";
	
	private static class AppXoidInfo {
		@SerializedName("available_fr") boolean availableInFrance;
		@SerializedName("rating") String raring;
		@SerializedName("app_updated") Date dateUpdated;
		@SerializedName("creator") String creator;
	}
	
	private static class AppXoid {
		@SerializedName("pk") int apkNum;
		@SerializedName("model") String model;
		@SerializedName("fields") AppXoidInfo data;
	}
	
	public void testGsonParser() throws Exception {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-mm-dd'T'HH:mm:ss").create();
		
		Type listType = new TypeToken<ArrayList<AppXoid>>() {}.getType();
		
		InputStreamGsonParser<ArrayList<AppXoid>> parser = new InputStreamGsonParser<ArrayList<AppXoid>>(gson, listType);
		HttpRequestGet request = new HttpRequestGet(JSON_URL);
		ArrayList<AppXoid> items = HttpClient.parseRequest(request, parser);
		assertNotNull(items);
	}
	
}
