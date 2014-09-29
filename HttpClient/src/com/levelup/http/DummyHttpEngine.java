package com.levelup.http;

/**
 * Created by robUx4 on 01/09/2014.
 */
public final class DummyHttpEngine<T, SE extends ServerException> implements HttpEngine<T, SE> {
	private final RawHttpRequest request;
	private final ResponseHandler<T, SE> responseHandler;

	public DummyHttpEngine(Builder<T, SE> builder) {
		this.request = builder.getHttpRequest();
		this.responseHandler = builder.getResponseHandler();
	}

	@Override
	public T call() throws HttpException, SE {
		throw new HttpUnsupportedException.Builder(request, null).build();
	}

	@Override
	public ResponseHandler<T, SE> getResponseHandler() {
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
	public String getHeader(String name) {
		return null;
	}

	@Override
	public HttpResponse getHttpResponse() {
		return null;
	}
}
