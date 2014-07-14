package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;

import android.net.Uri;

public class DelegateTypedHttpRequest<T> implements TypedHttpRequest<T> {
	
	protected final TypedHttpRequest<T> delegate;

	protected DelegateTypedHttpRequest(TypedHttpRequest<T> delegate) {
		if (null==delegate) throw new NullPointerException();
		this.delegate = delegate;
	}

	@Override
	public Uri getUri() {
		return delegate.getUri();
	}

	@Override
	public String getHttpMethod() {
		return delegate.getHttpMethod();
	}

	@Override
	public String getContentType() {
		return delegate.getContentType();
	}

	@Override
	public void addHeader(String name, String value) {
		delegate.addHeader(name, value);
	}

	@Override
	public void setHeader(String name, String value) {
		delegate.setHeader(name, value);
	}

	@Override
	public String getHeader(String name) {
		return delegate.getHeader(name);
	}

	@Override
	public RequestSigner getRequestSigner() {
		return delegate.getRequestSigner();
	}

	@Override
	public void settleHttpHeaders() throws HttpException {
		delegate.settleHttpHeaders();
	}

	@Override
	public void doConnection() throws IOException {
		delegate.doConnection();
	}

	@Override
	public void outputBody(OutputStream outputStream) throws IOException {
		delegate.outputBody(outputStream);
	}

	@Override
	public void setupBody() {
		delegate.setupBody();
	}

	@Override
	public void setResponse(HttpResponse resp) {
		delegate.setResponse(resp);
	}

	@Override
	public HttpResponse getResponse() {
		return delegate.getResponse();
	}

	@Override
	public LoggerTagged getLogger() {
		return delegate.getLogger();
	}

	@Override
	public HttpConfig getHttpConfig() {
		return delegate.getHttpConfig();
	}

	@Override
	public void setHttpConfig(HttpConfig config) {
		delegate.setHttpConfig(config);
	}

	@Override
	public Header[] getAllHeaders() {
		return delegate.getAllHeaders();
	}

	@Override
	public boolean hasBody() {
		return delegate.hasBody();
	}

	@Override
	public boolean isStreaming() {
		return delegate.isStreaming();
	}

	@Override
	public HttpException.Builder newException() {
		return delegate.newException();
	}

	@Override
	public HttpException.Builder newExceptionFromResponse(Throwable cause) {
		return delegate.newExceptionFromResponse(cause);
	}

	@Override
	public InputStreamParser<T> getInputStreamParser() {
		return delegate.getInputStreamParser();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
