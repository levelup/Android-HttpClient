package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;

import android.net.Uri;

import com.levelup.http.internal.HttpErrorHandler;

/**
 * Created by Dell990MT on 14/07/2014.
 */
public interface HttpEngine<T> extends TypedHttpRequest<T> {
	@Override
	String getHttpMethod();

	@Override
	InputStreamParser<T> getInputStreamParser();

	void setLogger(LoggerTagged logger);

	@Override
	LoggerTagged getLogger();

	@Override
	void setHttpConfig(HttpConfig config);

	@Override
	HttpConfig getHttpConfig();

	void prepareRequest(String userAgent) throws HttpException;

	@Override
	void settleHttpHeaders() throws HttpException;

	@Override
	void addHeader(String key, String value);

	@Override
	void setHeader(String key, String value);

	@Override
	String getHeader(String name);

	@Override
	String getContentType();

	Header[] getAllHeaders();

	@Override
	Uri getUri();

	@Override
	void outputBody(OutputStream outputStream) throws IOException;

	void setProgressListener(UploadProgressListener listener);

	UploadProgressListener getProgressListener();

	@Override
	boolean hasBody();

	@Override
	void setResponse(HttpResponse resp);

	@Override
	HttpResponse getResponse();

	RequestSigner getRequestSigner();

	@Override
	HttpException.Builder newException();

	@Override
	HttpException.Builder newExceptionFromResponse(Throwable cause);

	void setErrorHandler(HttpErrorHandler errorHandler);
}
