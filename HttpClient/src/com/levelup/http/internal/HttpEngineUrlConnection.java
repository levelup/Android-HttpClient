package com.levelup.http.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.os.Build;

import com.levelup.http.DataErrorException;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpConfig;
import com.levelup.http.HttpException;
import com.levelup.http.HttpExceptionFactory;
import com.levelup.http.LogManager;
import com.levelup.http.LoggerTagged;
import com.levelup.http.ParserException;
import com.levelup.http.RawHttpRequest;
import com.levelup.http.ResponseHandler;

/**
 * Basic HTTP request to be passed to {@link com.levelup.http.HttpClient}
 *
 * @param <T> type of the data read from the HTTP response
 * @see com.levelup.http.HttpRequestGet for a more simple API
 * @see com.levelup.http.HttpRequestPost for a more simple POST API
 */
public class HttpEngineUrlConnection<T> extends BaseHttpEngine<T,HttpResponseUrlConnection> {
	final HttpURLConnection urlConnection;

	public HttpEngineUrlConnection(RawHttpRequest request, ResponseHandler<T> responseHandler, HttpExceptionFactory exceptionFactory) {
		super(request, responseHandler, exceptionFactory);

		try {
			this.urlConnection = (HttpURLConnection) new URL(request.getUri().toString()).openConnection();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Bad uri: " + request.getUri(), e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Process the HTTP request on the network and return the HttpURLConnection
	 * @return an {@link java.net.HttpURLConnection} with the network response
	 * @throws com.levelup.http.HttpException
	 */
	private void getQueryResponse() throws HttpException {
		prepareRequest();

		try {
			final LoggerTagged logger = request.getLogger();
			if (null != logger) {
				logger.v(request.getHttpMethod() + ' ' + request.getUri());
				for (Map.Entry<String, List<String>> header : urlConnection.getRequestProperties().entrySet()) {
					logger.v(header.getKey()+": "+header.getValue());
				}
			}

			doConnection();

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
				HttpException.Builder builder = createExceptionBuilder();
				builder.setErrorMessage("Connection closed "+e.getMessage());
				builder.setCause(e);
				builder.setErrorCode(HttpException.ERROR_NETWORK);
				throw builder.build();
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void settleHttpHeaders() throws HttpException {
		try {
			urlConnection.setRequestMethod(request.getHttpMethod());

		} catch (ProtocolException e) {
			throw exceptionToHttpException(e).build();
		}

		final long contentLength;
		if (null != request.getBodyParams()) {
			setHeader(HTTP.CONTENT_TYPE, request.getBodyParams().getContentType());
			contentLength = request.getBodyParams().getContentLength();
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
		} else {
			contentLength = 0L;
		}
		setHeader(HTTP.CONTENT_LEN, Long.toString(contentLength));

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			urlConnection.setFixedLengthStreamingMode((int) contentLength);
		else
			urlConnection.setFixedLengthStreamingMode(contentLength);

		if (/*allowGzip && */request.getHeader(HttpClient.ACCEPT_ENCODING)==null) {
			setHeader(HttpClient.ACCEPT_ENCODING, "gzip,deflate");
		}

		super.settleHttpHeaders();

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
	public final void setupBody() {
		// do nothing
	}

	public final void doConnection() throws IOException {
		urlConnection.connect();

		if (null != request.getBodyParams()) {
			OutputStream output = urlConnection.getOutputStream();
			try {
				outputBody(output, request);
			} finally {
				output.close();
			}
		}
	}

	@Override
	protected HttpResponseUrlConnection queryResponse() throws HttpException {
		getQueryResponse();

		try {
			httpResponse.getInputStream();
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

	@Override
	protected T responseToResult(HttpResponseUrlConnection response) throws ParserException, IOException {
		return responseHandler.contentParser.transformData(response, this);
	}
}
