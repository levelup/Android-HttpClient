package com.levelup.http;

/**
 * Basic HTTP request to be passed to {@link HttpClient}
 * @see HttpRequestGet for a more simple API
 * @see HttpRequestPost for a more simple POST API
 * @param <T> type of the data read from the HTTP response
 */
public class BaseHttpRequest<T, SE extends ServerException> extends RawHttpRequest implements TypedHttpRequest<T, SE> {

	private final ResponseHandler<T, SE> responseHandler;

	public final static class Builder<T, SE extends ServerException> extends AbstractBuilder<T, SE, BaseHttpRequest<T, SE>, Builder<T, SE>> {
		public Builder() {
		}

		@Override
		protected BaseHttpRequest<T, SE> build(Builder<T, SE> builder) {
			return new BaseHttpRequest<T, SE>(builder);
		}
	}

	public abstract static class ChildBuilder<T, SE extends ServerException, REQ extends BaseHttpRequest<T, SE>> extends AbstractBuilder<T, SE, REQ, ChildBuilder<T,SE, REQ>> {
		public ChildBuilder() {
		}
	}

	public static abstract class AbstractBuilder<T, SE extends ServerException, REQ extends BaseHttpRequest<T, SE>, BUILDER extends RawHttpRequest.AbstractBuilder<REQ,BUILDER>> extends RawHttpRequest.AbstractBuilder<REQ, BUILDER> {
		private ResponseHandler<T, SE> responseHandler;

		protected AbstractBuilder() {
		}

		/**
		 * Set the parser that will be responsible for transforming the response body from the server into object {@code T}
		 * @param responseHandler HTTP response body parser
		 * @return Current Builder
		 */
		public BUILDER setResponseHandler(ResponseHandler<T, SE> responseHandler) {
			this.responseHandler = responseHandler;
			return (BUILDER) this;
		}

		public ResponseHandler<T, SE> getResponseHandler() {
			return responseHandler;
		}
	}

	protected BaseHttpRequest(AbstractBuilder<T,SE,?,?> builder) {
		super(builder);
		this.responseHandler = builder.responseHandler;
	}

	@Override
	public ResponseHandler<T, SE> getResponseHandler() {
		return responseHandler;
	}
}
