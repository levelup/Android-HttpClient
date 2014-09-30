package com.levelup.http;

import com.levelup.http.parser.XferTransform;

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
		private XferTransform<HttpResponse,T> contentParser;
		private XferTransform<HttpResponse,SE> errorParser;

		protected AbstractBuilder() {
		}

		/**
		 * Set the parser that will be responsible for transforming the response body from the server into object {@link T} or a server error into exception {@link SE}
		 * @param responseHandler HTTP response body parser
		 * @return Current Builder
		 * @see #setContentParser(com.levelup.http.parser.XferTransform)
		 * @see #setErrorParser(com.levelup.http.parser.XferTransform)
		 */
		public BUILDER setResponseHandler(ResponseHandler<T, SE> responseHandler) {
			this.responseHandler = responseHandler;
			return (BUILDER) this;
		}

		/**
		 * Set the parser that will be used to transform the response body into object {@link T}
		 * @param contentParser
		 * @return Current Builder
		 * @see #setErrorParser(com.levelup.http.parser.XferTransform)
		 * @see #setResponseHandler(ResponseHandler)
		 */
		public BUILDER setContentParser(XferTransform<HttpResponse, T> contentParser) {
			if (null!=responseHandler && responseHandler.contentParser != contentParser) throw new IllegalStateException("setResponseHandler() already called with another contentParser");
			this.contentParser = contentParser;
			return (BUILDER) this;
		}

		/**
		 * Set the parser that will be used to transform the response body into object {@link SE} when there is a server error
		 * @param errorParser
		 * @return Current Builder
		 * @see #setContentParser(com.levelup.http.parser.XferTransform)
		 * @see #setResponseHandler(ResponseHandler)
		 */
		public BUILDER setErrorParser(XferTransform<HttpResponse,SE> errorParser) {
			if (null!=responseHandler && responseHandler.errorParser != errorParser) throw new IllegalStateException("setResponseHandler() already called with another errorParser");
			this.errorParser = errorParser;
			return (BUILDER) this;
		}
	}

	protected BaseHttpRequest(AbstractBuilder<T,SE,?,?> builder) {
		super(builder);
		if (builder.responseHandler==null) {
			if (null==builder.contentParser) throw new NullPointerException("missing content parser");
			if (null==builder.errorParser) throw new NullPointerException("missing error parser");
			this.responseHandler = new ResponseHandler<T, SE>(builder.contentParser, builder.errorParser);
		} else {
			this.responseHandler = builder.responseHandler;
		}
	}

	@Override
	public ResponseHandler<T, SE> getResponseHandler() {
		return responseHandler;
	}
}
