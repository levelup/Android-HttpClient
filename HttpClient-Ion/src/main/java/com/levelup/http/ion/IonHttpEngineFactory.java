package com.levelup.http.ion;

import android.content.Context;

import com.levelup.http.DummyHttpEngine;
import com.levelup.http.HttpExceptionFactory;
import com.levelup.http.RawHttpRequest;
import com.levelup.http.HttpEngine;
import com.levelup.http.HttpEngineFactory;
import com.levelup.http.ResponseHandler;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonHttpEngineFactory implements HttpEngineFactory {

	private static IonHttpEngineFactory INSTANCE;

	private final Context context;

	public static IonHttpEngineFactory getInstance(Context context) {
		if (null == INSTANCE) {
			INSTANCE = new IonHttpEngineFactory(context);
		}
		return INSTANCE;
	}

	private IonHttpEngineFactory(Context context) {
		this.context = context;
	}

	@Override
	public <T> HttpEngine<T> createEngine(RawHttpRequest request, ResponseHandler<T> responseHandler, HttpExceptionFactory exceptionFactory) {
		if (!HttpEngineIon.canHandleXferTransform(responseHandler.contentParser))
			return new DummyHttpEngine<T>(request, responseHandler, exceptionFactory);

		if (!HttpEngineIon.errorCompatibleWithData(responseHandler))
			// Ion returns the data fully parsed so if we don't have common ground to parse the data and the error data, Ion can't handle the request
			return new DummyHttpEngine<T>(request, responseHandler, exceptionFactory);

		return new HttpEngineIon<T>(request, responseHandler, context, exceptionFactory);
	}
}
