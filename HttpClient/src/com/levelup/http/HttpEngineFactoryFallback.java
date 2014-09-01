package com.levelup.http;

/**
 * Created by robUx4 on 01/09/2014.
 */
public class HttpEngineFactoryFallback implements HttpEngineFactory {
	public final HttpEngineFactory mainFactory;
	public final HttpEngineFactory fallbackFactory;

	public HttpEngineFactoryFallback(HttpEngineFactory mainFactory, HttpEngineFactory fallbackFactory) {
		this.mainFactory = mainFactory;
		this.fallbackFactory = fallbackFactory;
	}

	@Override
	public <T> HttpEngine<T> createEngine(RawHttpRequest request, ResponseHandler<T> responseHandler, HttpExceptionFactory exceptionFactory) {
		HttpEngine<T> engine = mainFactory.createEngine(request, responseHandler, exceptionFactory);
		if (null == engine || engine instanceof DummyHttpEngine)
			return fallbackFactory.createEngine(request, responseHandler, exceptionFactory);
		return engine;
	}
}
