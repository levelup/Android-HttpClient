package com.levelup.http.ion.internal;

import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.future.SimpleFuture;
import com.koushikdutta.async.http.libcore.RawHeaders;
import com.koushikdutta.async.parser.AsyncParser;
import com.levelup.http.DataErrorException;
import com.levelup.http.ion.HttpEngineIon;

/**
 * Created by robUx4 on 19/08/2014.
 */
public class AsyncParserWithError<T, ERROR> implements AsyncParser<T> {

	private final AsyncParser<T> contentParser;
	private final AsyncParser<ERROR> errorParser;
	private final HttpEngineIon engineIon;

	public AsyncParserWithError(AsyncParser<T> contentParser, AsyncParser<ERROR> errorParser, HttpEngineIon engineIon) {
		if (null==contentParser) throw new NullPointerException("we need a parser for the content");
		if (null==engineIon) throw new NullPointerException("we need a ion engine");
		this.contentParser = contentParser;
		this.errorParser = errorParser;
		this.engineIon = engineIon;
	}

	@Override
	public Future<T> parse(DataEmitter emitter) {
		if (null != errorParser && null != engineIon.getHeaders()) {
			RawHeaders responseHeaders = engineIon.getHeaders();
			if (responseHeaders.getResponseCode() < 200 || responseHeaders.getResponseCode() >= 400) {
				// this is an error, parse with the errorParser
				final SimpleFuture<T> futureResult = new SimpleFuture<T>();

				Future<ERROR> errorFuture = errorParser.parse(emitter);
				if (errorFuture instanceof SimpleFuture) {
					SimpleFuture<ERROR> futureCallback = (SimpleFuture) errorFuture;
					final FutureCallback<ERROR> oldCallback = futureCallback.getCompletionCallback();
					futureCallback.setCallback(new FutureCallback<ERROR>() {
						@Override
						public void onCompleted(Exception e, ERROR result) {
							oldCallback.onCompleted(e, result);
							if (e!=null)
								futureResult.setComplete(e);
							else
								futureResult.setComplete(new DataErrorException(result));
						}
					});
				}
				return futureResult;
			}
		}

		return contentParser.parse(emitter);
	}

	@Override
	public void write(DataSink sink, T value, CompletedCallback completed) {
		contentParser.write(sink, value, completed);
	}
}
