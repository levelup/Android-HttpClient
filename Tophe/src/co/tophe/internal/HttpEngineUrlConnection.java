package co.tophe.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.os.Build;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import co.tophe.AbstractHttpEngine;
import co.tophe.HttpConfig;
import co.tophe.HttpException;
import co.tophe.HttpIOException;
import co.tophe.HttpRequest;
import co.tophe.ServerException;
import co.tophe.log.LogManager;
import co.tophe.log.LoggerTagged;
import co.tophe.parser.ParserException;

/**
 * Basic HTTP request to be passed to {@link co.tophe.TopheClient}
 *
 * @param <T> type of the data read from the HTTP response
 * @see co.tophe.HttpRequestGet for a more simple API
 * @see co.tophe.HttpRequestPost for a more simple POST API
 */
public class HttpEngineUrlConnection<T, SE extends ServerException> extends AbstractHttpEngine<T,HttpResponseUrlConnection, SE> {
	final HttpURLConnection urlConnection;
	private static final String ENGINE_SIGNATURE = null; // TODO we could give the OS version

	/**
	 * {@link javax.net.ssl.SSLSocketFactory} that disables {@code SSLv3}
	 */
	private static class TLSFactory extends SSLSocketFactory {
		private final SSLSocketFactory delegate;

		private TLSFactory(SSLSocketFactory delegate) {
			this.delegate = delegate;
		}

		@Override
		public String[] getDefaultCipherSuites() {
			return delegate.getDefaultCipherSuites();
		}

		@Override
		public String[] getSupportedCipherSuites() {
			return delegate.getSupportedCipherSuites();
		}

		private static Socket makeSocketSafe(Socket socket) {
			if (socket instanceof SSLSocket) {
				SSLSocket sslSocket = (SSLSocket) socket;
				List<String> protocols = new ArrayList<String>(Arrays.asList(sslSocket.getEnabledProtocols()));
				protocols.remove("SSLv3");
				if (!protocols.isEmpty()) {
					sslSocket.setEnabledProtocols(protocols.toArray(new String[protocols.size()]));
				} else {
					LogManager.getLogger().w("using SSLv3 as there's no other protocol available for "+socket);
				}
			}
			return socket;
		}

		@Override
		public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
			return makeSocketSafe(delegate.createSocket(s, host, port, autoClose));
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			return makeSocketSafe(delegate.createSocket(host, port));
		}

		@Override
		public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
			return makeSocketSafe(delegate.createSocket(host, port, localHost, localPort));
		}

		@Override
		public Socket createSocket(InetAddress host, int port) throws IOException {
			return makeSocketSafe(delegate.createSocket(host, port));
		}

		@Override
		public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
			return makeSocketSafe(delegate.createSocket(address, port, localAddress, localPort));
		}
	}

	public HttpEngineUrlConnection(Builder<T, SE> builder) {
		super(builder);

		try {
			this.urlConnection = (HttpURLConnection) new URL(request.getUri().toString()).openConnection();
			if (urlConnection instanceof HttpsURLConnection) {
				HttpsURLConnection sslConnection = (HttpsURLConnection) urlConnection;
				SSLSocketFactory sslFactory = sslConnection.getSSLSocketFactory();
				TLSFactory safeFactory = new TLSFactory(sslFactory);
				sslConnection.setSSLSocketFactory(safeFactory);
			}
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

		if (request.getHeader(HttpRequest.HEADER_ACCEPT_ENCODING)==null) {
			setHeader(HttpRequest.HEADER_ACCEPT_ENCODING, "gzip,deflate");
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
	protected HttpResponseUrlConnection queryResponse() throws HttpException, SE {
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
				HttpException.Builder builder = new HttpIOException.Builder(request, httpResponse);
				builder.setErrorMessage("Connection closed "+e.getMessage());
				builder.setCause(e);
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
				throw responseHandler.errorParser.transformData(httpResponse, this);

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
