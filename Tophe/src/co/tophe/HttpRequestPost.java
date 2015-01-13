package co.tophe;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.tophe.body.HttpBodyParameters;
import co.tophe.parser.XferTransform;

/**
 * Basic HTTP POST request to use with {@link TopheClient}
 *
 * @param <T> Type of the object returned from the request after parsing the response body.
 * @author Steve Lhomme
 * @see BaseHttpRequest
 * @see co.tophe.TopheClient#parseRequest(TypedHttpRequest)
 */
public class HttpRequestPost<T> extends BaseHttpRequest<T, ServerException> {
	public static abstract class AbstractBuilder<T, REQ extends HttpRequestPost<T>, BUILDER extends AbstractBuilder<T, REQ, BUILDER>> extends BaseHttpRequest.AbstractBuilder<T, ServerException, REQ, BUILDER> {
		public AbstractBuilder() {
			setHttpMethod("POST");
		}
	}

	public static abstract class ChildBuilder<T, REQ extends HttpRequestPost<T>> extends AbstractBuilder<T, REQ, ChildBuilder<T, REQ>> {
	}

	public final static class Builder<T> extends AbstractBuilder<T, HttpRequestPost<T>, Builder<T>> {
		@Override
		protected HttpRequestPost<T> build(Builder<T> builder) {
			return new HttpRequestPost<T>(builder);
		}
	}

	/**
	 * Constructor.
	 *
	 * @param url            URL to query
	 * @param bodyParams     body to send with the request, may be {@code null}.
	 * @param responseParser The response parser that will turn the response body into type {@link T} or a {@link co.tophe.ServerException}
	 */
	public HttpRequestPost(@NonNull String url, @Nullable HttpBodyParameters bodyParams, @NonNull XferTransform<HttpResponse, T> responseParser) {
		this(new Builder<T>().setBody(bodyParams).setUrl(url).setResponseHandler(new BaseResponseHandler<T>(responseParser)));
	}

	/**
	 * @param uri
	 * @param bodyParams     body to send with the request, may be {@code null}.
	 * @param responseParser The response parser that will turn the response body into type {@link T} or a {@link co.tophe.ServerException}
	 */
	public HttpRequestPost(@NonNull Uri uri, @Nullable HttpBodyParameters bodyParams, @NonNull XferTransform<HttpResponse, T> responseParser) {
		this(new Builder<T>().setBody(bodyParams).setUri(uri).setResponseHandler(new BaseResponseHandler<T>(responseParser)));
	}

	protected HttpRequestPost(AbstractBuilder<T, ?, ?> builder) {
		super(builder);
	}
}
