package com.levelup.http.ion;

import android.content.Context;

import com.levelup.http.HttpExceptionFactory;
import com.levelup.http.RawHttpRequest;
import com.levelup.http.HttpEngine;
import com.levelup.http.HttpEngineFactory;
import com.levelup.http.ResponseHandler;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonHttpEngineFactory implements HttpEngineFactory {
	private final HttpEngineFactory fallbackFactory;

	public IonHttpEngineFactory(HttpEngineFactory fallbackFactory) {
		this.fallbackFactory = fallbackFactory;
	}

	@Override
	public <T> HttpEngine<T> createEngine(RawHttpRequest request, ResponseHandler<T> responseHandler, Context context, HttpExceptionFactory exceptionFactory) {
		if (!HttpEngineIon.canHandleXferTransform(responseHandler.contentParser))
			return fallbackFactory.createEngine(request, responseHandler, context, request);

		if (!HttpEngineIon.errorCompatibleWithData(responseHandler))
			// Ion returns the data fully parsed so if we don't have common ground to parse the data and the error data, Ion can't handle the request
			return fallbackFactory.createEngine(request, responseHandler, context, request);

		return new HttpEngineIon<T>(request, responseHandler, context, exceptionFactory);
	}
}
