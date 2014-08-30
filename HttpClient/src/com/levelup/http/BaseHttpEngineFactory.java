package com.levelup.http;

import android.content.Context;

import com.levelup.http.internal.HttpEngineUrlConnection;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class BaseHttpEngineFactory implements HttpEngineFactory {

	private BaseHttpEngineFactory() {
	}

	public static BaseHttpEngineFactory instance = new BaseHttpEngineFactory();

	@Override
	public <T> HttpEngine<T> createEngine(RawHttpRequest request, ResponseHandler<T> responseHandler, Context context, HttpExceptionFactory exceptionFactory) {
		return new HttpEngineUrlConnection<T>(request, responseHandler, exceptionFactory);
	}
}
