package com.levelup.http;

import android.net.Uri;

import com.levelup.http.parser.ResponseToVoid;

/**
 * An Http GET request that doesn't follow redirections
 *
 * @author Steve Lhomme
 */
public class HttpRequestGetNoRedirect extends BaseHttpRequest<Void> {

	public static abstract class AbstractBuilder<R extends HttpRequestGetNoRedirect> extends BaseHttpRequest.AbstractBuilder<Void,R> {
		public AbstractBuilder() {
			setFollowRedirect(false);
		}
	}

	public final static class Builder extends AbstractBuilder<HttpRequestGetNoRedirect> {
		@Override
		protected final HttpRequestGetNoRedirect build(HttpEngine<Void> impl) {
			return new HttpRequestGetNoRedirect(impl);
		}
	}

	public HttpRequestGetNoRedirect(String baseUrl) {
		this(new Builder().setUrl(baseUrl).buildImpl());
	}

	public HttpRequestGetNoRedirect(String baseUrl, HttpUriParameters uriParams) {
		this(new Builder().setUrl(baseUrl, uriParams).setResponseParser(ResponseToVoid.RESPONSE_HANDLER).buildImpl());
	}

	public HttpRequestGetNoRedirect(Uri baseUri, HttpUriParameters uriParams) {
		this(new Builder().setUrl(baseUri.toString(), uriParams).setResponseParser(ResponseToVoid.RESPONSE_HANDLER).buildImpl());
	}

	protected HttpRequestGetNoRedirect(HttpEngine<Void> impl) {
		super(impl);
	}
}