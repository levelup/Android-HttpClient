package com.levelup.http;

import java.util.concurrent.Callable;

import android.content.Context;

/**
 * Created by Steve Lhomme on 14/07/2014.
 */
public interface HttpEngine<T> extends Callable<T>, ImmutableHttpRequest {
	/**
	 * Process the {@link com.levelup.http.HttpRequest} it was built with
	 * @return The result processed by the {@link com.levelup.http.ResponseHandler}
	 * @throws HttpException
	 */
	T call() throws HttpException;

	/**
	 * @return The {@link com.levelup.http.ResponseHandler} that will be used to parse the reponse body
	 */
	ResponseHandler<T> getResponseHandler();

	/**
	 * @return The {@link com.levelup.http.HttpExceptionFactory} used by the engine
	 */
	HttpExceptionFactory getExceptionFactory();

	/**
	 * Extra header to add to the query, in addition of the ones from the source {@link com.levelup.http.HttpRequest}
	 * <p>Can be used to sign a request with a timestamp, for example</p>
	 * @param name HTTP Header name
	 * @param value HTTP Header value
	 */
	void setHeader(String name, String value);

	public static class Builder<T> {
		private ResponseHandler<T> responseHandler;
		private RawHttpRequest httpRequest;
		private HttpEngineFactory factory = HttpClient.getHttpEngineFactory();

		public Builder() {
		}

		public Builder<T> setTypedRequest(TypedHttpRequest<T> request) {
			return setRequest(request)
					.setResponseHandler(request.getResponseHandler());
		}

		public Builder<T> setRequest(HttpRequest request) {
			if (!(request instanceof RawHttpRequest)) throw new IllegalStateException("only RawHttpRequest supported for now");
			this.httpRequest = (RawHttpRequest) request;
			return this;
		}

		public Builder<T> setResponseHandler(ResponseHandler<T> responseHandler) {
			this.responseHandler = responseHandler;
			return this;
		}

		public Builder<T> setHttpEngineFactory(HttpEngineFactory factory) {
			this.factory = factory;
			return this;
		}

		public HttpEngine<T> build() {
			if (null == httpRequest) throw new NullPointerException("missing a HttpRequest for the engine");
			if (null == responseHandler) throw new NullPointerException("missing a ResponseHandler for the engine of "+httpRequest);
			HttpEngine<T> httpEngine = factory.createEngine(httpRequest, responseHandler, httpRequest);
			if (null == httpEngine)
				return new DummyHttpEngine<T>(httpRequest, responseHandler, httpRequest);
			return httpEngine;
		}
	}
}
