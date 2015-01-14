package co.tophe.engine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.tophe.HttpEngine;
import co.tophe.HttpException;
import co.tophe.HttpNoEngineException;
import co.tophe.HttpRequestInfo;
import co.tophe.HttpResponse;
import co.tophe.RawHttpRequest;
import co.tophe.ResponseHandler;
import co.tophe.ServerException;

/**
 * @author Created by robUx4 on 01/09/2014.
 */
public final class DummyHttpEngine<T, SE extends ServerException> implements HttpEngine<T, SE> {
	private final RawHttpRequest request;
	private final ResponseHandler<T, SE> responseHandler;

	public DummyHttpEngine(Builder<T, SE> builder) {
		this.request = builder.getHttpRequest();
		this.responseHandler = builder.getResponseHandler();
	}

	@Override
	public T call() throws SE, HttpException {
		throw new HttpNoEngineException.Builder(request).build();
	}

	@NonNull
	@Override
	public ResponseHandler<T, SE> getResponseHandler() {
		return responseHandler;
	}

	@Override
	public HttpRequestInfo getHttpRequest() {
		return request;
	}

	@Override
	public void setHeader(@NonNull String name, @Nullable String value) {
		throw new AssertionError("not supported");
	}

	@NonNull
	@Override
	public String getHeader(String name) {
		throw new AssertionError("not supported");
	}

	@Override
	@Nullable
	public HttpResponse getHttpResponse() {
		return null;
	}
}
