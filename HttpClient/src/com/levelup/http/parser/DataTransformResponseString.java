package com.levelup.http.parser;

import java.io.InputStream;

import com.levelup.http.HttpResponse;

/**
 * Created by robUx4 on 20/08/2014.
 */
public class DataTransformResponseString extends DataTransformDual<HttpResponse, String, InputStream> {
	public static final DataTransformResponseString INSTANCE = new DataTransformResponseString();

	public DataTransformResponseString() {
		super(DataTransformResponseInputStream.INSTANCE, DataTransformInputStreamString.INSTANCE);
	}
}
