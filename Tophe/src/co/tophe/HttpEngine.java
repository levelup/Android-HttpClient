package co.tophe;

import java.util.concurrent.Callable;

import android.support.annotation.NonNull;

/**
 * @author Created by Steve Lhomme on 14/07/2014.
 */
public interface HttpEngine<T, SE extends ServerException> extends Callable<T>, ImmutableHttpRequest {
	/**
	 * Process the {@link HttpRequest} it was built with
	 * @return The result processed by the {@link ResponseHandler}
	 * @throws HttpException
	 * @throws SE
	 */
	T call() throws SE, HttpException;

	/**
	 * @return The {@link ResponseHandler} that will be used to parse the response body
	 */
	@NonNull
	ResponseHandler<T, SE> getResponseHandler();

	/**
	 * Extra header to add to the query, in addition of the ones from the source {@link HttpRequest}
	 * <p>Can be used to sign a request with a timestamp, for example</p>
	 * @param name HTTP Header name
	 * @param value HTTP Header value
	 */
	void setHeader(String name, String value);

	/**
	 * Get the internal header value used by the engine, may differ or not exist in the original {@link HttpRequest}
	 * @param name HTTP Header name
	 * @return HTTP Header value
	 */
	String getHeader(String name);

	public static class Builder<T, SE extends ServerException> {
		private ResponseHandler<T,SE> responseHandler;
		private RawHttpRequest httpRequest;
		private HttpEngineFactory factory = TopheClient.getHttpEngineFactory();
		private int threadStatsTag;

		public Builder() {
		}

		public Builder<T, SE> setTypedRequest(TypedHttpRequest<T, SE> request) {
			return setRequest(request)
					.setResponseHandler(request.getResponseHandler());
		}

		public Builder<T, SE> setRequest(@NonNull HttpRequest request) {
			if (null!=request && !(request instanceof RawHttpRequest)) throw new IllegalStateException("invalid RawRequest:"+request);
			this.httpRequest = (RawHttpRequest) request;
			return this;
		}

		public Builder<T, SE> setResponseHandler(@NonNull ResponseHandler<T, SE> responseHandler) {
			this.responseHandler = responseHandler;
			return this;
		}

		public Builder<T, SE> setHttpEngineFactory(HttpEngineFactory factory) {
			this.factory = factory;
			return this;
		}

		/**
		 * Set a tag to mark the query processed in this thread as belonging to a certain class of requests
		 * @param threadStatsTag the tag for the engine when it will run
		 * @return Current Builder
		 * @see android.net.TrafficStats
		 */
		public Builder<T, SE> setThreadStatsTag(int threadStatsTag) {
			this.threadStatsTag = threadStatsTag;
			return this;
		}

		public HttpEngine<T, SE> build() {
			if (null == httpRequest) throw new NullPointerException("missing a HttpRequest for the engine");
			if (null == responseHandler) throw new NullPointerException("missing a ResponseHandler for the engine of "+httpRequest);
			HttpEngine<T, SE> httpEngine = factory.createEngine(this);
			if (null == httpEngine)
				return new DummyHttpEngine<T, SE>(this);
			return httpEngine;
		}

		public RawHttpRequest getHttpRequest() {
			return httpRequest;
		}

		public ResponseHandler<T,SE> getResponseHandler() {
			return responseHandler;
		}

		public int getThreadStatsTag() {
			return threadStatsTag;
		}
	}
}
