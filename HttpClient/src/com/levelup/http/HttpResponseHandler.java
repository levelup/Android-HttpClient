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

	public final XferTransform<HttpResponse, OUTPUT> contentParser;
	public final XferTransform<HttpResponse, ERROR> errorParser;

	public ResponseParser(XferTransform<HttpResponse, OUTPUT> contentParser, XferTransform<HttpResponse, ERROR> errorParser) {
		if (null == contentParser) throw new NullPointerException("we need a parser for the content");
		this.contentParser = contentParser;
		this.errorParser = errorParser;
	}

	public ResponseParser(XferTransform<HttpResponse, OUTPUT> contentParser) {
		this(contentParser, null);
	}

	public OUTPUT parseResponse(ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException {
		if (null != errorParser && request.getHttpResponse().getResponseCode() < 200 || request.getHttpResponse().getResponseCode() >= 400) {
			ERROR errorContent = errorParser.transformData(request.getHttpResponse(), request);
			throw new DataErrorException(errorContent);
		}
		return contentParser.transformData(request.getHttpResponse(), request);
	}

}
