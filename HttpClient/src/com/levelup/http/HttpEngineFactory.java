package com.levelup.http;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public interface HttpEngineFactory {

	<T> HttpEngine<T> createEngine(RawHttpRequest request, ResponseHandler<T> responseHandler, HttpExceptionFactory exceptionFactory);

}
