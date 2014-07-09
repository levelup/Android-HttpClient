package com.levelup.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import com.levelup.http.internal.HttpResponseUrlConnection;

/**
 * HTTP client that handles {@link HttpRequest} 
 */
public class HttpClient {
	public static final String ACCEPT_ENCODING = "Accept-Encoding";

	private static HttpUrlConnectionFactory connectionFactory;
	private static String userAgent;
	private static CookieManager cookieManager;
	private static Header[] defaultHeaders;
	static Context defaultContext;

	/**
	 * Setup internal values of the {@link HttpClient} using the provided {@link Context}
	 * <p>The user agent is deduced from the app name of the {@code context} if it's not {@code null}</p>
	 * @param context Used to get a proper User Agent for your app, may be {@code null}
	 */
	public static void setup(Context context) {
		userAgent = "LevelUp-HttpClient/00000";
		if (null!=context) {
			defaultContext = context;
			PackageManager pM = context.getPackageManager();
			try {
				PackageInfo pI = pM.getPackageInfo(context.getPackageName(), 0);
				if (pI != null)
					userAgent = pI.applicationInfo.nonLocalizedLabel + "/" + pI.versionCode;
			} catch (NameNotFoundException ignored) {
			}
		}
	}

	public static void setConnectionFactory(HttpUrlConnectionFactory factory) {
		connectionFactory = factory;
	}

	public static void setCookieManager(CookieManager cookieManager) {
		HttpClient.cookieManager = cookieManager;
	}

	static CookieManager getCookieManager() {
		return cookieManager;
	}

	public static void setDefaultHeaders(Header[] headers) {
		defaultHeaders = headers;
	}

	public static Header[] getDefaultHeaders() {
		return defaultHeaders;
	}

	public static HttpURLConnection openURL(HttpRequest request) throws IOException {
		if (null != connectionFactory)
			return connectionFactory.createConnection(request);

		try {
			URL url = request.getURL();
			return (HttpURLConnection) url.openConnection();
		} catch (MalformedURLException e) {
			throw (IOException) new IOException("Malformed URL on:"+request).initCause(e);
		}
	}

	public static HttpURLConnection openURL(URL url) throws IOException {
		if (null != connectionFactory) {
			HttpRequest request = new BaseHttpRequest<Void>(url.toExternalForm());
			return connectionFactory.createConnection(request);
		}

		return (HttpURLConnection) url.openConnection();
	}

	/**
	 * Process the HTTP request on the network and return the HttpURLConnection
	 * @param request
	 * @return an {@link HttpURLConnection} with the network response
	 * @throws HttpException
	 */
	public static HttpURLConnection getQueryResponse(HttpRequest request) throws HttpException {
		return getQueryResponse(request, false);
	}


	/**
	 * Process the HTTP request on the network and return the HttpURLConnection
	 * @param request
	 * @return an {@link HttpURLConnection} with the network response
	 * @throws HttpException
	 */
	private static HttpURLConnection getQueryResponse(HttpRequest request, boolean allowGzip) throws HttpException {
		HttpURLConnection connection = null;
		try {
			connection = openURL(request);

			/*
			HttpResponse resp = null;
			try {
				HttpConnectionParams.setSoTimeout(client.getParams(), config.getReadTimeout(request));
				HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT_IN_MS);
			 */
			if (!TextUtils.isEmpty(userAgent))
				connection.setRequestProperty(HTTP.USER_AGENT, userAgent);

			if (null!=defaultHeaders) {
				for (Header header : defaultHeaders) {
					request.setHeader(header.getName(), header.getValue());
				}
			}

			if (null!=cookieManager) {
				cookieManager.setCookieHeader(request);
			}

			connection.setRequestMethod(request.getHttpMethod());
			request.settleHttpHeaders();
			request.setConnectionProperties(connection);

			if (allowGzip && connection.getRequestProperty(ACCEPT_ENCODING)==null) {
				connection.setRequestProperty(ACCEPT_ENCODING, "gzip,deflate");
			}

			final LoggerTagged logger = request.getLogger(); 
			if (null != logger) {
				logger.v(connection.getRequestMethod() + ' ' + request.getUri());
				for (Entry<String, List<String>> header : connection.getRequestProperties().entrySet()) {
					logger.v(header.getKey()+": "+header.getValue());
				}
			}

			if (null != request.getHttpConfig()) {
				int readTimeout = request.getHttpConfig().getReadTimeout(request);
				if (readTimeout>=0)
					connection.setReadTimeout(readTimeout);
			}

			connection.connect();

			request.outputBody(connection);

			if (null != logger) {
				logger.v(connection.getResponseMessage());
				for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
					logger.v(header.getKey()+": "+header.getValue());
				}
			}

		} catch (SecurityException e) {
			LogManager.getLogger().w("security error for "+request+' '+e);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Security error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			throw builder.build();

		} catch (SocketTimeoutException e) {
			LogManager.getLogger().d("timeout for "+request);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Timeout error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_TIMEOUT);
			throw builder.build();

		} catch (IOException e) {
			LogManager.getLogger().d("i/o error for "+request+' '+e.getMessage());
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("IO error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			throw builder.build();

		} finally {
			try {
				request.setResponse(new HttpResponseUrlConnection(connection));
			} catch (IllegalStateException e) {
				// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/689
				LogManager.getLogger().d("connection closed ? for "+request+' '+e);
				HttpException.Builder builder = request.newException();
				builder.setErrorMessage("Connection closed "+e.getMessage());
				builder.setCause(e);
				builder.setErrorCode(HttpException.ERROR_NETWORK);
				throw builder.build();
			}
		}
		return connection;
	}

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @return The InputStream corresponding to the data stream, may be null
	 * @throws HttpException
	 */
	public static InputStream getInputStream(HttpRequest request) throws HttpException {
		HttpURLConnection resp = getQueryResponse(request, true);

		InputStream is = null;
		if (resp!=null) {
			try {
				final int contentLength = resp.getContentLength();
				if (contentLength != 0) {
					is = resp.getInputStream();
					if ("deflate".equals(resp.getContentEncoding()) && !(is instanceof InflaterInputStream))
						is = new InflaterInputStream(is);
					if ("gzip".equals(resp.getContentEncoding()) && !(is instanceof GZIPInputStream))
						is = new GZIPInputStream(is);
				}

				if (resp.getResponseMessage()==null && null!=is) {
					String body = InputStreamStringParser.instance.parseInputStream(is, request);

					HttpException.Builder builder = request.newException();
					builder.setErrorMessage(TextUtils.isEmpty(body) ? "empty response" : body);
					builder.setErrorCode(HttpException.ERROR_HTTP);
					throw builder.build();
				}

				if (resp.getResponseCode() < 200 || resp.getResponseCode() >= 300) {
					HttpException.Builder builder = request.newExceptionFromResponse(null);
					builder.setErrorCode(HttpException.ERROR_HTTP);
					throw builder.build();
				}

				final String expectedMimeType = resp.getRequestProperty("Accept");
				if (!TextUtils.isEmpty(expectedMimeType)) {
					// test if it's the right MIME type or throw an exception that can be caught to use the bad data
					MediaType expectedType = MediaType.parse(expectedMimeType);
					if (null!=expectedType && !expectedType.equalsType(MediaType.parse(resp.getContentType()))) {
						String body = InputStreamStringParser.instance.parseInputStream(is, request);

						HttpException.Builder builder = request.newException();
						builder.setErrorMessage("Expected '"+expectedMimeType+"' got '"+resp.getContentType()+"' - "+body);
						builder.setErrorCode(HttpException.ERROR_HTTP_MIME);
						throw builder.build();
					}
				}

			} catch (FileNotFoundException e) {
				HttpException.Builder builder = request.newExceptionFromResponse(e);
				HttpException exception = builder.build();
				if (null==exception.getCause())
					LogManager.getLogger().d("http error "+exception.getMessage());
				else
					LogManager.getLogger().d("http error for "+request, e);
				throw exception;

			} catch (SocketTimeoutException e) {
				LogManager.getLogger().d("timeout for "+request);
				HttpException.Builder builder = request.newException();
				builder.setErrorMessage("timeout");
				builder.setCause(e);
				builder.setErrorCode(HttpException.ERROR_TIMEOUT);
				throw builder.build();

			} catch (IOException e) {
				LogManager.getLogger().d("i/o error for "+request+' '+e.getMessage());
				HttpException.Builder builder = request.newException();
				builder.setErrorMessage("IO error "+e.getMessage());
				builder.setCause(e);
				builder.setErrorCode(HttpException.ERROR_NETWORK);
				throw builder.build();
			}
		}

		return is;
	}

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @return The parsed object or null
	 * @throws HttpException
	 */
	public static <T> T parseRequest(TypedHttpRequest<T> request) throws HttpException {
		InputStreamParser<T> streamParser = request.getInputStreamParser();
		if (null==streamParser) throw new NullPointerException("typed request without a stream parser:"+request);
		return parseRequest(request, streamParser);
	}

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @param parser The {@link InputStreamParser parser} used to transform the input stream into the desired type. May be {@code null}
	 * @return The parsed object or null
	 * @throws HttpException
	 */
	public static <T> T parseRequest(HttpRequest request, InputStreamParser<T> parser) throws HttpException {
		InputStream is = getInputStream(request);
		if (null==is)
			return null;

		try {
			if (null!=parser)
				return parser.parseInputStream(is, request);
			else
				return null;

		} catch (SocketTimeoutException e) {
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("timeout");
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_TIMEOUT);
			throw builder.build();

		} catch (IOException e) {
			LogManager.getLogger().d("i/o error for "+request+' '+e.getMessage());
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("IO error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			throw builder.build();

		} catch (ParserException e) {
			LogManager.getLogger().i("incorrect data for "+request);
			if (e.getCause() instanceof HttpException)
				throw (HttpException) e.getCause();

			HttpException.Builder builder = request.newException();
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_PARSER);
			throw builder.build();

		} finally {
			if (!request.isStreaming()) {
				// the input stream data should have been all parsed
				try {
					is.close();
				} catch (NullPointerException ignored) {
					// okhttp 2.0 bug https://github.com/square/okhttp/issues/690
				} catch (IOException ignored) {
				}
			}
		}
	}

	/**
	 * Perform the query on the network and get the resulting body as a String
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @return The resulting body as a String
	 * @throws HttpException
	 */
	public static String getStringResponse(HttpRequest request) throws HttpException {
		return parseRequest(request, InputStreamStringParser.instance);
	}
}
