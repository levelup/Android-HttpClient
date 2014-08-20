package com.levelup.http.parser;

import java.io.IOException;

import com.levelup.http.DataErrorException;
import com.levelup.http.HttpResponse;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * Created by robUx4 on 20/08/2014.
 */
public class ResponseParser<OUTPUT, ERROR> {

	public final DataTransform<HttpResponse, OUTPUT> contentParser;
	public final DataTransform<HttpResponse, ERROR> errorParser;

	public ResponseParser(DataTransform<HttpResponse, OUTPUT> contentParser, DataTransform<HttpResponse, ERROR> errorParser) {
		if (null == contentParser) throw new NullPointerException("we need a parser for the content");
		this.contentParser = contentParser;
		this.errorParser = errorParser;
	}

	public ResponseParser(DataTransform<HttpResponse, OUTPUT> contentParser) {
		this(contentParser, null);
	}

	public OUTPUT parseResponse(ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException {
		if (null != errorParser && request.getHttpResponse().getResponseCode() < 200 || request.getHttpResponse().getResponseCode() >= 400) {
			ERROR errorContent = errorParser.transform(request.getHttpResponse(), request);
			throw new DataErrorException(errorContent);
		}
		return contentParser.transform(request.getHttpResponse(), request);
	}

}
