package co.tophe.parser;

import java.io.IOException;

import co.tophe.BaseResponseHandler;
import co.tophe.HttpResponse;
import co.tophe.ImmutableHttpRequest;

/**
 * Helper class to transform the HTTP response body into a {@link java.lang.Void}. In this case {@code null} is returned from the parser.
 * <p>Includes a static {@link #INSTANCE} and a default {@link #RESPONSE_HANDLER} that throws a raw/untyped
 * {@link co.tophe.ServerException ServerException}.</p>
 *
 * @author Created by robUx4 on 29/08/2014.
 */
public class BodyToVoid implements XferTransform<HttpResponse, Void> {
	/**
	 * The instance you should use when you want to discard the body of an HTTP request.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setContentParser(XferTransform) BaseHttpRequest.Builder.setContentParser()
	 */
	public static final BodyToVoid INSTANCE = new BodyToVoid();

	/**
	 * An instance to use when you want to discard the body of an HTTP request or throw a raw/untyped
	 * {@link co.tophe.ServerException ServerException} on server generated errors.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setResponseHandler(co.tophe.ResponseHandler) BaseHttpRequest.Builder.setResponseHandler()
	 */
	public static final BaseResponseHandler<Void> RESPONSE_HANDLER = new BaseResponseHandler<Void>(INSTANCE);

	private BodyToVoid() {
	}

	@Override
	public Void transformData(HttpResponse response, ImmutableHttpRequest request) throws IOException, ParserException {
		return null;
	}
}
