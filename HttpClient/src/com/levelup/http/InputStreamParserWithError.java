package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by robUx4 on 19/08/2014.
 */
public class InputStreamParserWithError<T, ERROR> implements InputStreamParser<T> {

	public final InputStreamParser<T> contentParser;
	public final InputStreamParser<ERROR> errorParser;

	public InputStreamParserWithError(InputStreamParser<T> contentParser, InputStreamParser<ERROR> errorParser) {
		if (null == contentParser) throw new NullPointerException("We need a content parser");
		this.contentParser = contentParser;
		this.errorParser = errorParser;
	}

	@Override
	public T parseInputStream(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException {
		if (null != errorParser && request.getHttpResponse().getResponseCode() < 200 || request.getHttpResponse().getResponseCode() >= 400) {
			ERROR errorContent = errorParser.parseInputStream(inputStream, request);
			throw new DataErrorException(errorContent);
		}
		return contentParser.parseInputStream(inputStream, request);
	}

	@Override
	public GsonStreamParser<T> getGsonParser() {
		return contentParser.getGsonParser();
	}
}
