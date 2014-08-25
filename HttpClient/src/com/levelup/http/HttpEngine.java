package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.levelup.http.internal.HttpErrorHandler;
import com.levelup.http.parser.ResponseParser;

/**
 * Created by Steve Lhomme on 14/07/2014.
 */
public interface HttpEngine<T> extends HttpRequestInfo {
	ResponseParser<T,?> getResponseParser();

	void setLogger(LoggerTagged logger);

	LoggerTagged getLogger();

	void setHttpConfig(HttpConfig config);

	HttpConfig getHttpConfig();

	void addHeader(String key, String value);

	void setHeader(String key, String value);

	void setProgressListener(UploadProgressListener listener);

	UploadProgressListener getProgressListener();

	HttpException.Builder newException();

	HttpException.Builder newExceptionFromResponse(Throwable cause);

	void setErrorHandler(HttpErrorHandler errorHandler);

	void doConnection() throws IOException;

	void setupBody();

	<P> P parseRequest(ResponseParser<P,?> parser, HttpRequest request) throws HttpException;

	InputStream getInputStream(HttpRequest request, ResponseParser<?, ?> parser) throws HttpException;

	void settleHttpHeaders(HttpRequest request) throws HttpException;

	void outputBody(OutputStream outputStream, HttpRequestInfo requestInfo) throws IOException;

	HttpResponse getResponse();
}
