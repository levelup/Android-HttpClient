package co.tophe;

import android.net.Uri;

import co.tophe.parser.XferTransform;

/**
 * Basic HTTP GET request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 * @see BaseHttpRequest for a more complete API
 */
public class HttpRequestGet<T> extends BaseHttpRequest<T, ServerException> {
	public static abstract class AbstractBuilder<T, REQ extends HttpRequestGet<T>, BUILDER extends AbstractBuilder<T,REQ,BUILDER>> extends BaseHttpRequest.AbstractBuilder<T,ServerException,REQ,BUILDER> {
	}

	public static abstract class ChildBuilder<T, REQ extends HttpRequestGet<T>> extends AbstractBuilder<T, REQ, ChildBuilder<T, REQ>> {
	}

	public final static class Builder<T> extends AbstractBuilder<T,HttpRequestGet<T>,Builder<T>> {
		@Override
		protected HttpRequestGet<T> build(Builder<T> builder) {
			return new HttpRequestGet<T>(builder);
		}
	}

	public HttpRequestGet(String baseUrl, HttpUriParameters uriParams, XferTransform<HttpResponse, T> responseParser) {
		this(new Builder<T>().setUrl(baseUrl, uriParams).setResponseHandler(new BaseResponseHandler<T>(responseParser)));
	}

	public HttpRequestGet(Uri baseUri, HttpUriParameters uriParams, XferTransform<HttpResponse, T> responseParser) {
		this(new Builder<T>().setUrl(baseUri.toString(), uriParams).setResponseHandler(new BaseResponseHandler<T>(responseParser)));
	}

	public HttpRequestGet(String url, XferTransform<HttpResponse, T> responseParser) {
		this(new Builder<T>().setUrl(url).setResponseHandler(new BaseResponseHandler<T>(responseParser)));
	}

	protected HttpRequestGet(AbstractBuilder<T,?,?> builder) {
		super(builder);
	}
}
