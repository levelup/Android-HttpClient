package co.tophe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.http.protocol.HTTP;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import co.tophe.body.HttpBodyParameters;
import co.tophe.log.LoggerTagged;
import co.tophe.signed.AbstractRequestSigner;
import co.tophe.signed.RequestSigner;

/**
 * Base class of all {@link co.tophe.HttpRequest}.
 *
 * @author Created by robUx4 on 29/08/2014.
 */
public class RawHttpRequest implements HttpRequest {
	/**
	 * Builder for a {@link RawHttpRequest RawHttpRequest}
	 */
	public final static class Builder extends AbstractBuilder<RawHttpRequest, Builder> {

		/**
		 * Constructor for the {@link RawHttpRequest} builder, setting {@code GET} method by default
		 */
		public Builder() {
			super();
		}

		@Override
		protected RawHttpRequest build(Builder builder) {
			return new RawHttpRequest(builder);
		}
	}

	/**
	 * Builder for a child class of {@link RawHttpRequest} that doesn't need its own builder
	 *
	 * @param <REQ> Type of the child class of {@link RawHttpRequest}
	 */
	public static abstract class ChildBuilder<REQ extends RawHttpRequest> extends AbstractBuilder<REQ, ChildBuilder<REQ>> {

		/**
		 * Constructor for the {@link REQ} builder, setting {@code GET} method by default
		 */
		public ChildBuilder() {
			super();
		}
	}

	/**
	 * Abstract Builder for a {@link RawHttpRequest RawHttpRequest} child instance.
	 *
	 * @param <R> type of the HTTP request class returned by {@link #build()}
	 */
	public static abstract class AbstractBuilder<R extends RawHttpRequest, B extends AbstractBuilder<R, ? extends B>> {
		private static final String DEFAULT_HTTP_METHOD = "GET";
		private static final String DEFAULT_POST_METHOD = "POST";

		private HttpBodyParameters bodyParams;
		private Uri uri;
		private String httpMethod;
		private RequestSigner signer;

		/**
		 * Constructor for the {@link RawHttpRequest} builder, setting {@code GET} method by default
		 */
		public AbstractBuilder() {
		}

		/**
		 * Set the class that will be responsible to send the HTTP body of the query
		 * <p>sets {@code POST} method by default
		 *
		 * @param bodyParams the object that will write the HTTP body to the remote server
		 * @return Current Builder
		 */
		public B setBody(@Nullable HttpBodyParameters bodyParams) {
			return setBody(null, bodyParams);
		}

		/**
		 * Set the class that will be responsible to send the HTTP body of the query
		 *
		 * @param postMethod HTTP method to use with this request, {@code GET} and {@code HEAD} not possible
		 * @param bodyParams the object that will write the HTTP body to the remote server
		 * @return Current Builder
		 * @see {@link #setHttpMethod(String)}
		 */
		public B setBody(@Nullable String postMethod, @Nullable HttpBodyParameters bodyParams) {
			setHttpMethod(postMethod);
			if (null != bodyParams && httpMethod != null && !isMethodWithBody(httpMethod))
				throw new IllegalArgumentException("invalid body for HTTP method:" + httpMethod);
			this.bodyParams = bodyParams;
			return (B) this;
		}

		/**
		 * Sets the HTTP method to use for the request like {@code GET}, {@code POST} or {@code HEAD}
		 *
		 * @param httpMethod HTTP method to use with this request
		 * @return Current Builder
		 */
		public B setHttpMethod(@Nullable String httpMethod) {
			if (!TextUtils.isEmpty(httpMethod)) {
				if (null != bodyParams && !isMethodWithBody(httpMethod))
					throw new IllegalArgumentException("invalid HTTP method with body:" + httpMethod);
				this.httpMethod = httpMethod;
			}
			return (B) this;
		}

		/**
		 * Set the URL that will be queried on the remote server
		 *
		 * @param url requested on the server
		 * @return Current Builder
		 */
		public B setUrl(String url) {
			return setUrl(url, null);
		}

		/**
		 * Set the URL that will be queried on the remote server
		 *
		 * @param url       requested on the server
		 * @param uriParams parameters to add to the URL
		 * @return Current Builder
		 */
		public B setUrl(String url, @Nullable HttpUriParameters uriParams) {
			if (url == null)
				this.uri = null;
			else {
				Uri uri = Uri.parse(url);
				if (null == uriParams) {
					this.uri = uri;
				} else {
					Uri.Builder uriBuilder = uri.buildUpon();
					uriParams.appendUriParameters(uriBuilder);
					this.uri = uriBuilder.build();
				}
			}
			return (B) this;
		}

		/**
		 * Set the URL that will be queried on the remote server
		 *
		 * @param uri requested on the server
		 * @return Current Builder
		 */
		public B setUri(Uri uri) {
			this.uri = uri;
			return (B) this;
		}

		public Uri getUri() {
			return uri;
		}

		/**
		 * Set the object that will be responsible for signing the {@link HttpRequest}
		 *
		 * @param signer object that will sign the {@link HttpRequest}
		 * @return Current Builder
		 */
		public B setSigner(RequestSigner signer) {
			if (null == signer) {
				throw new IllegalArgumentException();
			}
			this.signer = signer;
			return (B) this;
		}

		public RequestSigner getSigner() {
			return signer;
		}

		public String getHttpMethod() {
			if (TextUtils.isEmpty(httpMethod)) {
				return null != bodyParams ? DEFAULT_POST_METHOD : DEFAULT_HTTP_METHOD;
			}
			return httpMethod;
		}

		public HttpBodyParameters getBodyParams() {
			return bodyParams;
		}

		/**
		 * Build the {@link R} instance
		 * <p>ONLY IMPLEMENT IN A NON ABSTRACT Builder
		 *
		 * @return
		 */
		protected abstract R build(B builder);

		/**
		 * Build the HTTP request to run through {@link TopheClient}
		 */
		public R build() {
			return build((B) this);
		}
	}

	private static boolean isMethodWithBody(String httpMethod) {
		return !TextUtils.equals(httpMethod, "GET") && !TextUtils.equals(httpMethod, "HEAD");
	}

	private final Uri uri;
	private final String httpMethod;
	private final HttpBodyParameters bodyParams;
	private final RequestSigner signer;
	private HttpConfig httpConfig = BasicHttpConfig.INSTANCE;
	private LoggerTagged loggerTagged;
	private UploadProgressListener progressListener;

	protected final Map<String, String> mRequestSetHeaders = new HashMap<String, String>();
	protected final Map<String, HashSet<String>> mRequestAddHeaders = new HashMap<String, HashSet<String>>();

	protected RawHttpRequest(AbstractBuilder builder) {
		this.uri = builder.getUri();
		this.httpMethod = builder.getHttpMethod();
		this.signer = builder.getSigner();
		this.bodyParams = builder.getBodyParams();

		if (!TextUtils.isEmpty(TopheClient.getUserAgent())) {
			mRequestSetHeaders.put(HTTP.USER_AGENT, TopheClient.getUserAgent());
		}

		if (!TextUtils.isEmpty(TopheClient.getXRequestedWith())) {
			mRequestSetHeaders.put("X-Requested-With", TopheClient.getXRequestedWith());
		}

		final Header[] defaultHeaders = TopheClient.getDefaultHeaders();
		if (null != defaultHeaders) {
			for (Header defaultHeader : defaultHeaders) {
				mRequestSetHeaders.put(defaultHeader.getName(), defaultHeader.getValue());
			}
		}
	}

	@Override
	public Uri getUri() {
		return uri;
	}

	@Override
	public String getHttpMethod() {
		return httpMethod;
	}

	@Override
	public HttpBodyParameters getBodyParameters() {
		return bodyParams;
	}

	// TODO move this in the AbstractBuilder ?
	@Override
	public void addHeader(String key, String value) {
		HashSet<String> values = mRequestAddHeaders.get(key);
		if (null == values) {
			values = new HashSet<String>();
			mRequestAddHeaders.put(key, values);
		}
		values.add(value);
	}

	// TODO move this in the AbstractBuilder ?
	@Override
	public void setHeader(String key, String value) {
		mRequestAddHeaders.remove(key);
		if (null == value)
			mRequestSetHeaders.remove(key);
		else
			mRequestSetHeaders.put(key, value);
	}

	@Override
	public String getHeader(String name) {
		if (mRequestSetHeaders.containsKey(name))
			return mRequestSetHeaders.get(name);
		if (mRequestAddHeaders.containsKey(name)) {
			HashSet<String> values = mRequestAddHeaders.get(name);
			if (!values.isEmpty())
				return values.iterator().next();
		}
		return null;
	}

	@Override
	public LoggerTagged getLogger() {
		return loggerTagged;
	}

	@Override
	public HttpConfig getHttpConfig() {
		return httpConfig;
	}

	@Override
	public void setHttpConfig(HttpConfig config) {
		this.httpConfig = config;
	}

	@Override
	public Header[] getAllHeaders() {
		List<Header> headers = new ArrayList<Header>(mRequestSetHeaders.size() + mRequestAddHeaders.size());
		for (Map.Entry<String, String> setHeader : mRequestSetHeaders.entrySet()) {
			headers.add(new Header(setHeader.getKey(), setHeader.getValue()));
		}
		for (Map.Entry<String, HashSet<String>> entries : mRequestAddHeaders.entrySet()) {
			for (String entry : entries.getValue()) {
				headers.add(new Header(entries.getKey(), entry));
			}
		}
		return headers.toArray(new Header[headers.size()]);
	}

	/**
	 * Set a specific logger for this request.
	 */
	// TODO move this to the engine ?
	public void setLogger(LoggerTagged loggerTagged) {
		this.loggerTagged = loggerTagged;
	}

	/**
	 * Set the progress listener for this request. Useful when sending a large body in the request.
	 */
	public void setProgressListener(UploadProgressListener listener) {
		this.progressListener = listener;
	}

	public UploadProgressListener getProgressListener() {
		return progressListener;
	}

	@Override
	public RequestSigner getRequestSigner() {
		return signer;
	}

	/**
	 * Get extra information about the request to show in logs. You may override this to make sure some API keys don't end up in the logs.
	 */
	protected String getToStringExtra() {
		String result = getUri() == null ? "" : getUri().toString();
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
				simpleName = simpleName.substring(end + 1);
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
