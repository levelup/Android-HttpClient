package com.levelup.http;

/**
 * Basic HTTP request to be passed to {@link HttpClient}
 * @see HttpRequestGet for a more simple API
 * @see HttpRequestPost for a more simple POST API
 * @param <T> type of the data read from the HTTP response
 */
public class BaseHttpRequest<T> extends RawHttpRequest implements TypedHttpRequest<T> {

	private final ResponseHandler<T> responseHandler;

	public final static class Builder<T> extends AbstractBuilder<T, BaseHttpRequest<T>, Builder<T>> {
		public Builder() {
		}

		@Override
		protected BaseHttpRequest<T> build(Builder<T> builder) {
			return new BaseHttpRequest<T>(builder);
		}
	}

	public abstract static class ChildBuilder<T, REQ extends BaseHttpRequest<T>> extends AbstractBuilder<T, REQ, ChildBuilder<T,REQ>> {
		public ChildBuilder() {
		}
	}

	public static abstract class AbstractBuilder<T, REQ extends BaseHttpRequest<T>, BUILDER extends RawHttpRequest.AbstractBuilder<REQ,BUILDER>> extends RawHttpRequest.AbstractBuilder<REQ, BUILDER> {
		private ResponseHandler<T> responseHandler;

		protected AbstractBuilder() {
		}

		/**
		 * Set the parser that will be responsible for transforming the response body from the server into object {@code T}
		 * @param responseHandler HTTP response body parser
		 * @return Current Builder
		 */
		public BUILDER setResponseParser(ResponseHandler<T> responseHandler) {
			this.responseHandler = responseHandler;
			return (BUILDER) this;
		}

		public ResponseHandler<T> getResponseHandler() {
			return responseHandler;
		}
	}

	protected BaseHttpRequest(AbstractBuilder<T,?,?> builder) {
		super(builder);
		this.responseHandler = builder.responseHandler;
	}

	@Override
	public ResponseHandler<T> getResponseHandler() {
		return responseHandler;
	}
}
