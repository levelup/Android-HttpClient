package co.tophe;

import android.support.annotation.NonNull;

import co.tophe.parser.XferTransform;

/**
 * Basic HTTP request to be passed to {@link TopheClient} and {@link co.tophe.async.AsyncTopheClient}
 *
 * @param <T>  type of the data read from the HTTP response.
 * @param <SE> type of the Exception raised for all server generated errors.
 * @see co.tophe.BaseHttpRequest.AbstractBuilder
 * @see HttpRequestGet HttpRequestGet for a more simple API
 * @see HttpRequestPost HttpRequestPost for a more simple POST API
 */
public class BaseHttpRequest<T, SE extends ServerException> extends RawHttpRequest implements TypedHttpRequest<T, SE> {

	@NonNull
	private final ResponseHandler<T, SE> responseHandler;

	/**
	 * Builder to build a plain {@link co.tophe.BaseHttpRequest}.
	 *
	 * @param <T>  type of the data read from the HTTP response.
	 * @param <SE> type of the Exception raised for all server generated errors.
	 */
	public final static class Builder<T, SE extends ServerException> extends AbstractBuilder<T, SE, BaseHttpRequest<T, SE>, Builder<T, SE>> {
		public Builder() {
		}

		@Override
		protected BaseHttpRequest<T, SE> build(Builder<T, SE> builder) {
			return new BaseHttpRequest<T, SE>(builder);
		}
	}

	/**
	 * Builder where you can specify the exact type of the {@link co.tophe.BaseHttpRequest}
	 *
	 * @param <T>   type of the data read from the HTTP response.
	 * @param <SE>  type of the Exception raised for all server generated errors.
	 * @param <REQ> type of the generated {@link co.tophe.BaseHttpRequest} when calling {@link #build()}.
	 */
	public abstract static class ChildBuilder<T, SE extends ServerException, REQ extends BaseHttpRequest<T, SE>> extends AbstractBuilder<T, SE, REQ, ChildBuilder<T, SE, REQ>> {
		public ChildBuilder() {
		}
	}

	/**
	 * Builder where you can specify the exact type of the {@link co.tophe.BaseHttpRequest} that will be built and using a custom "typed" builder.
	 *
	 * @param <T>       type of the data read from the HTTP response.
	 * @param <SE>      type of the Exception raised for all server generated errors.
	 * @param <REQ>     type of the generated {@link co.tophe.BaseHttpRequest} when calling {@link #build()}.
	 * @param <BUILDER> type of the Builder used to generate type {@link REQ}.
	 */
	public static abstract class AbstractBuilder<T, SE extends ServerException, REQ extends BaseHttpRequest<T, SE>, BUILDER extends RawHttpRequest.AbstractBuilder<REQ, BUILDER>> extends RawHttpRequest.AbstractBuilder<REQ, BUILDER> {
		private ResponseHandler<T, SE> responseHandler;
		private XferTransform<HttpResponse, T> contentParser;
		private XferTransform<HttpResponse, SE> errorParser;

		protected AbstractBuilder() {
		}

		/**
		 * Set the parser that will be responsible for transforming the response body from the server into object {@link T} or a server error into exception {@link SE}
		 *
		 * @param responseHandler HTTP response body parser
		 * @return Current Builder
		 * @see #setContentParser(co.tophe.parser.XferTransform)
		 * @see #setErrorParser(co.tophe.parser.XferTransform)
		 * @see co.tophe.parser.BodyToJSONObject#RESPONSE_HANDLER
		 * @see co.tophe.parser.BodyToJSONArray#RESPONSE_HANDLER
		 * @see co.tophe.parser.BodyToHttpStream#RESPONSE_HANDLER
		 * @see co.tophe.parser.BodyToString#RESPONSE_HANDLER
		 */
		public BUILDER setResponseHandler(@NonNull ResponseHandler<T, SE> responseHandler) {
			this.responseHandler = responseHandler;
			return (BUILDER) this;
		}

		/**
		 * Set the parser that will be used to transform the response body into object {@link T}
		 *
		 * @param contentParser
		 * @return Current Builder
		 * @see #setErrorParser(co.tophe.parser.XferTransform)
		 * @see #setResponseHandler(ResponseHandler)
		 * @see co.tophe.gson.BodyViaGson
		 * @see co.tophe.parser.BodyToJSONObject#INSTANCE
		 * @see co.tophe.parser.BodyToJSONArray#INSTANCE
		 * @see co.tophe.parser.BodyToHttpStream#INSTANCE
		 * @see co.tophe.parser.BodyToString#INSTANCE
		 */
		public BUILDER setContentParser(@NonNull XferTransform<HttpResponse, T> contentParser) {
			if (null!=responseHandler && responseHandler.contentParser != contentParser) throw new IllegalStateException("setResponseHandler() already called with another contentParser");
			this.contentParser = contentParser;
			return (BUILDER) this;
		}

		/**
		 * Set the parser that will be used to transform the response body into object {@link SE} when there is a server error
		 *
		 * @param errorParser
		 * @return Current Builder
		 * @see #setContentParser(co.tophe.parser.XferTransform)
		 * @see #setResponseHandler(ResponseHandler)
		 */
		public BUILDER setErrorParser(@NonNull XferTransform<HttpResponse, SE> errorParser) {
			if (null!=responseHandler && responseHandler.errorParser != errorParser) throw new IllegalStateException("setResponseHandler() already called with another errorParser");
			this.errorParser = errorParser;
			return (BUILDER) this;
		}
	}

	protected BaseHttpRequest(AbstractBuilder<T, SE, ?, ?> builder) {
		super(builder);
		if (builder.responseHandler == null) {
			if (null == builder.contentParser) throw new NullPointerException("missing content parser");
			if (null == builder.errorParser) throw new NullPointerException("missing error parser");
			this.responseHandler = new ResponseHandler<T, SE>(builder.contentParser, builder.errorParser);
		} else {
			this.responseHandler = builder.responseHandler;
		}
	}

	@NonNull
	@Override
	public ResponseHandler<T, SE> getResponseHandler() {
		return responseHandler;
	}
}
