package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.levelup.http.internal.HttpErrorHandler;
import com.levelup.http.internal.HttpRequestIon;
import com.levelup.http.internal.HttpRequestUrlConnection;
import com.levelup.http.signed.AbstractRequestSigner;

/**
 * Basic HTTP request to be passed to {@link HttpClient}
 * @see HttpRequestGet for a more simple API 
 * @see HttpRequestPost for a more simple POST API
 * @param <T> type of the data read from the HTTP response
 */
public class BaseHttpRequest<T> extends DelegateTypedHttpRequest<T> implements HttpErrorHandler {
	/** Object to tell we are not outputting an object but using streaming data */
	private static final InputStreamParser<HttpStream> streamingRequest = new InputStreamParser<HttpStream>() {
		@Override
		public HttpStream parseInputStream(InputStream inputStream, HttpRequest request) throws IOException, ParserException {
			throw new IllegalAccessError("this parser should not be used");
		}
	};

	/**
	 * Builder for 
	 * @param <T> type of the data read from the HTTP response
	 */
	public static class Builder<T> {
		private static final String DEFAULT_HTTP_METHOD = "GET";
		private static final String DEFAULT_POST_METHOD = "POST";

		private Context context;
		private HttpBodyParameters bodyParams;
		private Uri uri;
		private InputStreamParser<T> streamParser;
		private String httpMethod = "GET";
		private RequestSigner signer;
		private Boolean followRedirect;

		/**
		 * Constructor for the {@link BaseHttpRequest} builder, setting {@code GET} method by default
		 */
		public Builder() {
			this(HttpClient.defaultContext);
		}

		/**
		 * Constructor for the {@link BaseHttpRequest} builder, setting {@code GET} method by default
		 */
		public Builder(Context context) {
			setContext(context);
			setHttpMethod(DEFAULT_HTTP_METHOD);
		}

		public Context getContext() {
			return context;
		}

		/**
		 * Set the class that will be responsible to send the HTTP body of the query
		 * <p>sets {@code POST} method by default
		 * @param bodyParams the object that will write the HTTP body to the remote server
		 * @return Current Builder
		 */
		public Builder<T> setBody(HttpBodyParameters bodyParams) {
			return setBody(DEFAULT_POST_METHOD, bodyParams);
		}

		/**
		 * Set the class that will be responsible to send the HTTP body of the query
		 * @param postMethod HTTP method to use with this request, {@code GET} and {@code HEAD} not possible
		 * @param bodyParams the object that will write the HTTP body to the remote server
		 * @return Current Builder
		 * @see {@link #setHttpMethod(String)}
		 */
		public Builder<T> setBody(String postMethod, HttpBodyParameters bodyParams) {
			setHttpMethod(postMethod);
			if (null!=bodyParams && httpMethod!=null && !isMethodWithBody(httpMethod))
				throw new IllegalArgumentException("invalid body for HTTP method:"+httpMethod);
			this.bodyParams = bodyParams;
			return this;
		}

		/**
		 * Sets the HTTP method to use for the request like {@code GET}, {@code POST} or {@code HEAD}
		 * @param httpMethod HTTP method to use with this request
		 * @return Current Builder
		 */
		public Builder<T> setHttpMethod(String httpMethod) {
			if (TextUtils.isEmpty(httpMethod))
				throw new IllegalArgumentException("invalid null HTTP method");
			if (null!=bodyParams && !isMethodWithBody(httpMethod))
				throw new IllegalArgumentException("invalid HTTP method with body:"+httpMethod);
			this.httpMethod = httpMethod;
			return this;
		}

		/**
		 * Set the URL that will be queried on the remote server
		 * @param url requested on the server
		 * @return Current Builder
		 */
		public Builder<T> setUrl(String url) {
			return setUrl(url, null);
		}

		/**
		 * Set the URL that will be queried on the remote server
		 * @param url requested on the server
		 * @param uriParams parameters to add to the URL
		 * @return Current Builder
		 */
		public Builder<T> setUrl(String url, HttpUriParameters uriParams) {
			Uri uri = Uri.parse(url);
			if (null==uriParams) {
				this.uri = uri;
			} else {
				Uri.Builder uriBuilder = uri.buildUpon();
				uriParams.addUriParameters(uriBuilder);
				this.uri = uriBuilder.build();
			}
			return this;
		}

		/**
		 * Set the URL that will be queried on the remote server
		 * @param uri requested on the server
		 * @return Current Builder
		 */
		public Builder<T> setUri(Uri uri) {
			this.uri = uri;
			return this;
		}

		/**
		 * Set the parser that will be responsible for transforming the response body from the server into object {@code T}
		 * @param streamParser HTTP response body parser
		 * @return Current Builder
		 */
		public Builder<T> setStreamParser(InputStreamParser<T> streamParser) {
			if (streamParser==streamingRequest)
				throw new IllegalArgumentException("Trying to set a stream parser on a streaming request");
			this.streamParser = streamParser;
			return this;
		}

		/**
		 * Indicate that this query will be used as a continuous stream rather than outputting an Object 
		 * @return Current Builder
		 */
		@SuppressWarnings("unchecked")
		public Builder<HttpStream> setStreaming() {
			if (streamParser!=null && streamParser!=streamingRequest)
				throw new IllegalArgumentException("Trying to set a streaming request that has a streaming parser");
			this.streamParser = (InputStreamParser<T>) streamingRequest;
			return (Builder<HttpStream>) this;
		}

		/**
		 * Set the object that will be responsible for signing the {@link HttpRequest}
		 * @param signer object that will sign the {@link HttpRequest}
		 * @return Current Builder
		 */
		public Builder<T> setSigner(RequestSigner signer) {
			if (null==signer) {
				throw new IllegalArgumentException();
			}
			this.signer = signer;
			return this;
		}

		public Builder<T> setContext(Context context) {
			this.context = context;
			return this;
		}

		/**
		 * Enable/Disable the HTTP redirection following, will follow redirections by default
		 * @param followRedirect
		 * @return Current Builder
		 */
		public Builder<T> setFollowRedirect(boolean followRedirect) {
			this.followRedirect = followRedirect;
			return this;
		}

		public Uri getUri() {
			return uri;
		}

		public String getHttpMethod() {
			return httpMethod;
		}

		public InputStreamParser<T> getInputStreamParser() {
			return streamParser;
		}

		public RequestSigner getSigner() {
			return signer;
		}

		public HttpBodyParameters getBodyParams() {
			return bodyParams;
		}

		public Boolean getFollowRedirect() {
			return followRedirect;
		}

		public BaseHttpRequest<T> build(HttpRequestImpl<T> impl) {
			return new BaseHttpRequest<T>(impl);
		}

		/**
		 * Build the HTTP request to run through {@link HttpClient}
		 */
		public BaseHttpRequest<T> build() {
			final HttpRequestImpl<T> impl;
			if (streamParser == streamingRequest)
				impl = new HttpRequestUrlConnection<T>(this);
			else
				impl = new HttpRequestIon<T>(this);
			BaseHttpRequest<T> result = build(impl);
			impl.setErrorHandler(result);
			return result;
		}
	}

	private static boolean isMethodWithBody(String httpMethod) {
		return !TextUtils.equals(httpMethod, "GET") && !TextUtils.equals(httpMethod, "HEAD");
	}

	protected BaseHttpRequest(HttpRequestImpl<T> delegate) {
		super(delegate);
	}

	protected BaseHttpRequest(Builder<T> builder) {
		super(builder.build());
	}

	@Override
	public boolean isStreaming() {
		return delegate.getInputStreamParser() == streamingRequest;
	}

	/**
	 * Handle error data returned in JSON format
	 * @param builder
	 * @param jsonData
	 * @return
	 */
	public HttpException.Builder handleJSONError(HttpException.Builder builder, JSONObject jsonData) {
		return builder;
	}

	HttpRequestImpl<T> getHttpRequestImpl() {
		if (delegate instanceof HttpRequestImpl)
			return (HttpRequestImpl<T>) delegate;
		if (delegate instanceof BaseHttpRequest)
			return ((BaseHttpRequest) delegate).getHttpRequestImpl();
		throw new IllegalStateException("invalid http request type");
	}

	public void setLogger(LoggerTagged loggerTagged) {
		getHttpRequestImpl().setLogger(loggerTagged);
	}

	public void setProgressListener(UploadProgressListener listener) {
		getHttpRequestImpl().setProgressListener(listener);
	}

	public UploadProgressListener getProgressListener() {
		return getHttpRequestImpl().getProgressListener();
	}

	public RequestSigner getRequestSigner() {
		return getHttpRequestImpl().getRequestSigner();
	}

	protected String getToStringExtra() {
		String result = getUri().toString();
		if (getRequestSigner() instanceof AbstractRequestSigner)
			result += " for " + ((AbstractRequestSigner) getRequestSigner()).getOAuthUser();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		String simpleName = getClass().getSimpleName();
		if (simpleName == null || simpleName.length() <= 0) {
			simpleName = getClass().getName();
			int end = simpleName.lastIndexOf('.');
			if (end > 0) {
				simpleName = simpleName.substring(end+1);
			}
		}
		sb.append(simpleName);
		sb.append('{');
		sb.append(Integer.toHexString(System.identityHashCode(this)));
		sb.append(' ');
		sb.append(getToStringExtra());
		sb.append('}');
		return sb.toString();
	}

}
