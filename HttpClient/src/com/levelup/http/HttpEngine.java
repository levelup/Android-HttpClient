package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.levelup.http.internal.HttpErrorHandler;

/**
 * Created by Steve Lhomme on 14/07/2014.
 */
public interface HttpEngine<T, R extends HttpResponse> extends HttpRequestInfo {
	ResponseHandler<T> getResponseHandler();

	void setLogger(LoggerTagged logger);

	LoggerTagged getLogger();

	void setHttpConfig(HttpConfig config);

	HttpConfig getHttpConfig();

	void addHeader(String key, String value);

	void setHeader(String key, String value);

	void setProgressListener(UploadProgressListener listener);

	UploadProgressListener getProgressListener();

	HttpException.Builder newException();

	void setErrorHandler(HttpErrorHandler errorHandler);

	void doConnection() throws IOException;

	void setupBody();

	T parseRequest(ResponseHandler<T> responseHandler, TypedHttpRequest<T> request) throws HttpException;

	InputStream getInputStream(TypedHttpRequest<T> request, ResponseHandler<T> responseHandler) throws HttpException;

	void settleHttpHeaders(TypedHttpRequest<T> request) throws HttpException;

	void outputBody(OutputStream outputStream, HttpRequestInfo requestInfo) throws IOException;

	R getResponse();
}
