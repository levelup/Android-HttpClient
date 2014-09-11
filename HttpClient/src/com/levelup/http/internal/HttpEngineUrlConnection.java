package com.levelup.http.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.os.Build;

import com.levelup.http.AbstractHttpEngine;
import com.levelup.http.DataErrorException;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpConfig;
import com.levelup.http.HttpException;
import com.levelup.http.log.LogManager;
import com.levelup.http.log.LoggerTagged;
import com.levelup.http.parser.ParserException;

/**
 * Basic HTTP request to be passed to {@link com.levelup.http.HttpClient}
 *
 * @param <T> type of the data read from the HTTP response
 * @see com.levelup.http.HttpRequestGet for a more simple API
 * @see com.levelup.http.HttpRequestPost for a more simple POST API
 */
public class HttpEngineUrlConnection<T> extends AbstractHttpEngine<T,HttpResponseUrlConnection> {
	final HttpURLConnection urlConnection;
	private static final String ENGINE_SIGNATURE = null; // TODO we could give the OS version

	public HttpEngineUrlConnection(Builder<T> builder) {
		super(builder);

		try {
			this.urlConnection = (HttpURLConnection) new URL(request.getUri().toString()).openConnection();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Bad uri: " + request.getUri(), e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		try {
			urlConnection.setRequestMethod(request.getHttpMethod());

		} catch (ProtocolException e) {
			throw new IllegalStateException(e);
		}

		if (request.getHeader(HttpClient.ACCEPT_ENCODING)==null) {
			setHeader(HttpClient.ACCEPT_ENCODING, "gzip,deflate");
		}
	}

	@Override
	protected String getEngineSignature() {
		return null;
	}

	@SuppressLint("NewApi")
	@Override
	protected void setContentLength(long contentLength) {
		super.setContentLength(contentLength);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			urlConnection.setFixedLengthStreamingMode((int) contentLength);
		else
			urlConnection.setFixedLengthStreamingMode(contentLength);
	}

	@Override
	public void setHeadersAndConfig() {
		for (Entry<String, String> entry : requestHeaders.entrySet()) {
			urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
		}

		if (null != responseHandler.followsRedirect()) {
			urlConnection.setInstanceFollowRedirects(responseHandler.followsRedirect());
		}

		HttpConfig httpConfig = request.getHttpConfig();
		if (null != httpConfig) {
			int readTimeout = httpConfig.getReadTimeout(request);
			if (readTimeout >= 0)
				urlConnection.setReadTimeout(readTimeout);
		}
	}

	@Override
	protected HttpResponseUrlConnection queryResponse() throws HttpException {
		try {
			final LoggerTagged logger = request.getLogger();
			if (null != logger) {
				logger.v(request.getHttpMethod() + ' ' + request.getUri());
				for (Map.Entry<String, List<String>> header : urlConnection.getRequestProperties().entrySet()) {
					logger.v(header.getKey()+": "+header.getValue());
				}
			}

			if (null != request.getBodyParameters()) {
				urlConnection.setDoOutput(true);
			}
			urlConnection.setDoInput(true);

			urlConnection.connect();

			if (null != request.getBodyParameters()) {
				OutputStream output = urlConnection.getOutputStream();
				try {
					outputBody(output, request);
				} finally {
					output.close();
				}
			}

			if (null != logger) {
				logger.v(urlConnection.getResponseMessage());
				for (Map.Entry<String, List<String>> header : urlConnection.getHeaderFields().entrySet()) {
					logger.v(header.getKey()+": "+header.getValue());
				}
			}

		} catch (SecurityException e) {
			throw exceptionToHttpException(e).build();

		} catch (IOException e) {
			throw exceptionToHttpException(e).build();

		} finally {
			try {
				setRequestResponse(new HttpResponseUrlConnection(this));
			} catch (IllegalStateException e) {
				// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/689
				LogManager.getLogger().d("connection closed ? for "+request+' '+e);
				HttpException.Builder builder = getExceptionFactory().newException(httpResponse);
				builder.setErrorMessage("Connection closed "+e.getMessage());
				builder.setCause(e);
				builder.setErrorCode(HttpException.ERROR_NETWORK);
				throw builder.build();
			}
		}

		try {
			InputStream inputStream = httpResponse.getInputStream();
			if (null == inputStream)
				throw exceptionToHttpException(new IOException("no inputStream")).build();

			return httpResponse;
		} catch (FileNotFoundException e) {
			try {
				DataErrorException exceptionWithData = responseHandler.errorHandler.handleError(httpResponse, this);

				HttpException.Builder exceptionBuilder = exceptionToHttpException(exceptionWithData);
				throw exceptionBuilder.build();

			} catch (ParserException ee) {
				throw exceptionToHttpException(ee).build();

			} catch (IOException ee) {
				throw exceptionToHttpException(ee).build();
			}
		} catch (IOException e) {
			throw exceptionToHttpException(e).build();

		}
	}
}
