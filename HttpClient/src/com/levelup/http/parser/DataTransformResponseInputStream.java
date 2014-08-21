package com.levelup.http.parser;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.DataErrorException;
import com.levelup.http.HttpResponse;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class DataTransformResponseInputStream implements DataTransform<HttpResponse,InputStream> {
	// This is a special class as it depends on the engine used, unlike other DataTransform

	public static final DataTransformResponseInputStream INSTANCE = new DataTransformResponseInputStream();

	private DataTransformResponseInputStream() {
	}

	@Override
	public InputStream transform(HttpResponse response, ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException {
		return response.getContentStream();
	}
}
