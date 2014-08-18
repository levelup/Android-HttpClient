package com.levelup.http;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
* Created by robUx4 on 8/1/2014.
*/
public interface GsonStreamParser<T> {
	Gson getGsonHandler();

	Type getGsonOutputType();

	TypeToken getGsonOutputTypeToken();

	T transformResult(Object gsonResult);
}
