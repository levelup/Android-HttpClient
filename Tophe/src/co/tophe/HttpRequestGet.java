package co.tophe;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.tophe.parser.XferTransform;

/**
 * Basic HTTP GET request to use with {@link TopheClient}
 *
 * @param <T> Type of the object returned from the request after parsing the response body.
 * @author Steve Lhomme
 * @see BaseHttpRequest
 * @see co.tophe.TopheClient#parseRequest(TypedHttpRequest)
 */
public class HttpRequestGet<T> extends BaseHttpRequest<T, ServerException> {
	public static abstract class AbstractBuilder<T, REQ extends HttpRequestGet<T>, BUILDER extends AbstractBuilder<T, REQ, BUILDER>> extends BaseHttpRequest.AbstractBuilder<T, ServerException, REQ, BUILDER> {
	}

	public static abstract class ChildBuilder<T, REQ extends HttpRequestGet<T>> extends AbstractBuilder<T, REQ, ChildBuilder<T, REQ>> {
	}

	public final static class Builder<T> extends AbstractBuilder<T, HttpRequestGet<T>, Builder<T>> {
		@Override
		protected HttpRequestGet<T> build(Builder<T> builder) {
			return new HttpRequestGet<T>(builder);
		}
	}

	/**
	 * @param baseUrl        URL to query
	 * @param uriParams      additional parameters to pass in the URL
	 * @param responseParser The response parser that will turn the response body into type {@link T} or a {@link co.tophe.ServerException}
	 */
	public HttpRequestGet(@NonNull String baseUrl, @Nullable HttpUriParameters uriParams, @NonNull XferTransform<HttpResponse, T> responseParser) {
		this(new Builder<T>().setUrl(baseUrl, uriParams).setResponseHandler(new BaseResponseHandler<T>(responseParser)));
	}

	/**
	 * @param baseUri        URL to query
	 * @param uriParams      additional parameters to pass in the URL
	 * @param responseParser The response parser that will turn the response body into type {@link T} or a {@link co.tophe.ServerException}
	 */
	public HttpRequestGet(@NonNull Uri baseUri, @Nullable HttpUriParameters uriParams, @NonNull XferTransform<HttpResponse, T> responseParser) {
		this(new Builder<T>().setUrl(baseUri.toString(), uriParams).setResponseHandler(new BaseResponseHandler<T>(responseParser)));
	}

	/**
	 * @param url            URL to query
	 * @param responseParser The response parser that will turn the response body into type {@link T} or a {@link co.tophe.ServerException}
	 */
	public HttpRequestGet(@NonNull String url, @NonNull XferTransform<HttpResponse, T> responseParser) {
		this(url, null, responseParser);
	}

	protected HttpRequestGet(AbstractBuilder<T, ?, ?> builder) {
		super(builder);
	}
}
