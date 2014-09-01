package com.levelup.http;

import com.levelup.http.internal.HttpEngineUrlConnection;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class BaseHttpEngineFactory implements HttpEngineFactory {

	private BaseHttpEngineFactory() {
	}

	public static final BaseHttpEngineFactory INSTANCE = new BaseHttpEngineFactory();

	@Override
	public <T> HttpEngine<T> createEngine(RawHttpRequest request, ResponseHandler<T> responseHandler, HttpExceptionFactory exceptionFactory) {
		return new HttpEngineUrlConnection<T>(request, responseHandler, exceptionFactory);
	}
}
