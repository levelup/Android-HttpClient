package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.net.Uri;

import com.levelup.http.internal.HttpErrorHandler;

/**
 * Created by Steve Lhomme on 14/07/2014.
 */
public interface HttpEngine<T> extends HttpRequestInfo {
	InputStreamParser<T> getInputStreamParser();

	void setLogger(LoggerTagged logger);

	LoggerTagged getLogger();

	void setHttpConfig(HttpConfig config);

	HttpConfig getHttpConfig();

	void addHeader(String key, String value);

	void setHeader(String key, String value);

	void setProgressListener(UploadProgressListener listener);

	UploadProgressListener getProgressListener();

	void setResponse(HttpResponse resp);

	HttpResponse getHttpResponse();

	HttpException.Builder newException();

	HttpException.Builder newExceptionFromResponse(Throwable cause);

	void setErrorHandler(HttpErrorHandler errorHandler);

	void doConnection() throws IOException;

	void setupBody();

	<P> P parseRequest(InputStreamParser<P> parser, HttpRequest request) throws HttpException;

	InputStream getInputStream(HttpRequest request) throws HttpException;

	void settleHttpHeaders(HttpRequest request) throws HttpException;

	void outputBody(OutputStream outputStream, HttpRequestInfo requestInfo) throws IOException;
}
