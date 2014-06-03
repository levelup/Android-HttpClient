package com.levelup.http.signed;


/**
 * @deprecated use {@link BaseHttpRequestSigned} 
 */
@Deprecated
public class HttpRequestSignedPost<T> extends BaseHttpRequestSigned<T> {

	public static class Builder<T> extends BaseHttpRequestSigned.Builder<T> {
		public Builder() {
			setHttpMethod("POST");
		}

		public HttpRequestSignedPost<T> build() {
			return new HttpRequestSignedPost<T>(this);
		}
	}

	/**
	 * @deprecated use {@link BaseHttpRequestSigned} 
	 */
	protected HttpRequestSignedPost(Builder<T> builder) {
		super(builder);
	}
}
