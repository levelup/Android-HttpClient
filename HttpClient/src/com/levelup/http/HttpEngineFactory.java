package com.levelup.http;

import android.content.Context;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public interface HttpEngineFactory {

	<T> HttpEngine<T> createEngine(RawHttpRequest request, ResponseHandler<T> responseHandler, Context context, HttpExceptionFactory exceptionFactory);

}
