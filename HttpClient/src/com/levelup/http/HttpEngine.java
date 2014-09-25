package com.levelup.http;

import java.util.concurrent.Callable;

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
	 * Extra header to add to the query, in addition of the ones from the source {@link com.levelup.http.HttpRequest}
	 * <p>Can be used to sign a request with a timestamp, for example</p>
	 * @param name HTTP Header name
	 * @param value HTTP Header value
	 */
	void setHeader(String name, String value);

	/**
	 * Get the internal header value used by the engine, may differ or not exist in the original {@link com.levelup.http.HttpRequest}
	 * @param name HTTP Header name
	 * @return HTTP Header value
	 */
	String getHeader(String name);

	public static class Builder<T> {
		private ResponseHandler<T> responseHandler;
		private RawHttpRequest httpRequest;
		private HttpEngineFactory factory = HttpClient.getHttpEngineFactory();
		private int threadStatsTag;

		public Builder() {
		}

		public Builder<T> setTypedRequest(TypedHttpRequest<T> request) {
			return setRequest(request)
					.setResponseHandler(request.getResponseHandler());
		}

		public Builder<T> setRequest(HttpRequest request) {
			if (null!=request && !(request instanceof RawHttpRequest)) throw new IllegalStateException("invalid RawRequest:"+request);
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

		/**
		 * Set a tag to mark the query processed in this thread as belonging to a certain class of requests
		 * @param threadStatsTag the tag for the engine when it will run, {@code null} by default
		 * @return Current Builder
		 * @see android.net.TrafficStats
		 */
		public Builder<T> setThreadStatsTag(int threadStatsTag) {
			this.threadStatsTag = threadStatsTag;
			return this;
		}

		public HttpEngine<T> build() {
			if (null == httpRequest) throw new NullPointerException("missing a HttpRequest for the engine");
			if (null == responseHandler) throw new NullPointerException("missing a ResponseHandler for the engine of "+httpRequest);
			HttpEngine<T> httpEngine = factory.createEngine(this);
			if (null == httpEngine)
				return new DummyHttpEngine<T>(this);
			return httpEngine;
		}

		public RawHttpRequest getHttpRequest() {
			return httpRequest;
		}

		public ResponseHandler<T> getResponseHandler() {
			return responseHandler;
		}

		public int getThreadStatsTag() {
			return threadStatsTag;
		}
	}
}
