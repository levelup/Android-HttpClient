package com.levelup.http;

import java.util.concurrent.Callable;

import android.content.Context;

/**
 * Created by Steve Lhomme on 14/07/2014.
 */
public interface HttpEngine<T> extends Callable<T>, ImmutableHttpRequest {
	T call() throws HttpException;

	ResponseHandler<T> getResponseHandler();

	/**
	 * Extra header to add to the query, in addition of the ones from the source {@link com.levelup.http.HttpRequest}
	 * <p>Can be used to sign a request with a timestamp, for example</p>
	 * @param key
	 * @param value
	 */
	void setHeader(String key, String value);

	HttpException.Builder createExceptionBuilder();

	public static class Builder<T> {
		private ResponseHandler<T> responseHandler;
		private RawHttpRequest httpRequest;
		private Context context = HttpClient.defaultContext;
		private HttpEngineFactory factory = HttpClient.getHttpEngineFactory();

		public Builder() {
		}

		public Builder<T> setTypedRequest(TypedHttpRequest<T> request) {
			return setRequest(request)
					.setResponseHandler(request.getResponseHandler())
					.setContext(request.getContext());
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

		public Builder<T> setContext(Context context) {
			this.context = context;
			return this;
		}

		public Builder<T> setHttpEngineFactory(HttpEngineFactory factory) {
			this.factory = factory;
			return this;
		}

		public HttpEngine<T> build() {
			if (null == httpRequest) throw new NullPointerException("missing a HttpRequest for the engine");
			if (null == responseHandler) throw new NullPointerException("missing a ResponseHandler for the engine of "+httpRequest);
			HttpEngine<T> httpEngine = factory.createEngine(httpRequest, responseHandler, context, httpRequest);
			return httpEngine;
		}
	}
}
