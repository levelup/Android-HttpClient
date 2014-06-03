package com.levelup.http.signed;


/**
 * @deprecated use {@link BaseHttpRequestSigned} 
 */
@Deprecated
public class HttpRequestSignedGet<T> extends BaseHttpRequestSigned<T> {

	public static class Builder<T> extends BaseHttpRequestSigned.Builder<T> {
		public HttpRequestSignedGet<T> build() {
			return new HttpRequestSignedGet<T>(this);
		}
    }

	/**
	 * @deprecated use {@link BaseHttpRequestSigned} 
	 */
	protected HttpRequestSignedGet(Builder<T> builder) {
		super(builder);
	}
}
