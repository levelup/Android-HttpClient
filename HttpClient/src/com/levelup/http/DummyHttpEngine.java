package com.levelup.http;

/**
 * Created by robUx4 on 01/09/2014.
 */
public final class DummyHttpEngine<T> implements HttpEngine<T> {
	private final RawHttpRequest request;
	private final ResponseHandler<T> responseHandler;
	private final HttpExceptionFactory exceptionFactory;

	public DummyHttpEngine(RawHttpRequest request, ResponseHandler<T> responseHandler, HttpExceptionFactory exceptionFactory) {
		this.request = request;
		this.responseHandler = responseHandler;
		this.exceptionFactory = exceptionFactory;
	}

	@Override
	public T call() throws HttpException {
		throw exceptionFactory.newException(null).setErrorCode(HttpException.ERROR_ENGINE).build();
	}

	@Override
	public HttpExceptionFactory getExceptionFactory() {
		return exceptionFactory;
	}

	@Override
	public ResponseHandler<T> getResponseHandler() {
		return responseHandler;
	}

	@Override
	public HttpRequestInfo getHttpRequest() {
		return request;
	}

	@Override
	public void setHeader(String name, String value) {
		throw new IllegalStateException();
	}

	@Override
	public HttpResponse getHttpResponse() {
		return null;
	}
}
