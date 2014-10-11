package co.tophe;

import android.net.Uri;

import co.tophe.body.HttpBodyParameters;
import co.tophe.parser.XferTransform;

/**
 * Basic HTTP POST request to use with {@link HttpClient}
 * 
 * @author Steve Lhomme
 * @see BaseHttpRequest for a more complete API
 */
public class HttpRequestPost<T> extends BaseHttpRequest<T, ServerException> {
	public static abstract class AbstractBuilder<T, REQ extends HttpRequestPost<T>, BUILDER extends AbstractBuilder<T,REQ,BUILDER>> extends BaseHttpRequest.AbstractBuilder<T,ServerException, REQ,BUILDER> {
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

	public HttpRequestPost(String url, HttpBodyParameters bodyParams, XferTransform<HttpResponse, T> responseParser) {
		this(new Builder<T>().setBody(bodyParams).setUrl(url).setResponseHandler(new BaseResponseHandler<T>(responseParser)));
	}

	public HttpRequestPost(Uri uri, HttpBodyParameters bodyParams, XferTransform<HttpResponse, T> responseParser) {
		this(new Builder<T>().setBody(bodyParams).setUri(uri).setResponseHandler(new BaseResponseHandler<T>(responseParser)));
	}

	protected HttpRequestPost(AbstractBuilder<T,?,?> builder) {
		super(builder);
	}
}
