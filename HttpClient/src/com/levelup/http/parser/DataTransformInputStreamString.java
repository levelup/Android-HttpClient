package com.levelup.http.parser;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.DataErrorException;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * Created by robUx4 on 20/08/2014.
 */
public final class DataTransformInputStreamString implements DataTransform<InputStream,String> {
	public static final DataTransformInputStreamString INSTANCE = new DataTransformInputStreamString();

	private DataTransformInputStreamString() {
	}

	@Override
	public String transform(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException {
		return null; // TODO
	}
}
