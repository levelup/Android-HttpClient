package com.levelup.http.parser;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.HttpStream;
import com.levelup.http.ImmutableHttpRequest;

/**
 * Created by robUx4 on 29/08/2014.
 */
public class XferTransformInputStreamHttpStream implements XferTransform<InputStream, HttpStream> {

	public static final XferTransformInputStreamHttpStream INSTANCE = new XferTransformInputStreamHttpStream();

	private XferTransformInputStreamHttpStream() {
	}

	@Override
	public HttpStream transformData(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException {
		return new HttpStream(inputStream, request);
	}
}
