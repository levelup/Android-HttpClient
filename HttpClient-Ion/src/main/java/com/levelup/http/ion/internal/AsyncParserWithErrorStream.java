package com.levelup.http.ion.internal;

import java.io.InputStream;

import com.koushikdutta.async.parser.AsyncParser;
import com.levelup.http.BaseHttpResponseErrorHandler;
import com.levelup.http.ion.HttpEngineIon;

/**
 * Created by robUx4 on 19/08/2014.
 */
public class AsyncParserWithErrorStream<T> extends AsyncParserWithError<T, InputStream> {

	public AsyncParserWithErrorStream(AsyncParser<T> contentParser, HttpEngineIon engineIon) {
		super(contentParser, BaseHttpResponseErrorHandler.INSTANCE, engineIon);
	}

}
