package com.levelup.http;

/**
 * Created by robUx4 on 01/09/2014.
 */
public class DummyHttpEngine<T> implements HttpEngine<T> {
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
		throw createExceptionBuilder().build();
	}

	@Override
	public HttpException.Builder createExceptionBuilder() {
		return exceptionFactory.newException(null).setErrorCode(HttpException.ERROR_ENGINE);
	}

	@Override
	public ResponseHandler<T> getResponseHandler() {
		return responseHandler;
	}

	@Override
	public HttpRequest getHttpRequest() {
		return request;
	}

	@Override
	public void setHeader(String key, String value) {
		throw new IllegalStateException();
	}

	@Override
	public HttpResponse getHttpResponse() {
		return null;
	}
}
