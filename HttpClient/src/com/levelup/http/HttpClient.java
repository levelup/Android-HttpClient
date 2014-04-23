package com.levelup.http;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

/**
 * HTTP client that handles {@link HttpRequest} 
 */
public class HttpClient {
	private static HttpUrlConnectionFactory connectionFactory;
	private static String userAgent;
	private static CookieManager cookieManager;
	private static Header[] defaultHeaders;

	/**
	 * Setup internal values of the {@link HttpClient} using the provided {@link Context}
	 * <p>The user agent is deduced from the app name of the {@code context} if it's not {@code null}</p>
	 * @param context Used to get a proper User Agent for your app, may be {@code null}
	 */
	public static void setup(Context context) {
		userAgent = "LevelUp-HttpClient/00000";
		if (null!=context) {
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

	public static HttpURLConnection openURL(URL url) throws IOException {
		if (null != connectionFactory)
			return connectionFactory.createConnection(url);
		else
			return (HttpURLConnection) url.openConnection();
	}

	/**
	 * Process the HTTP request on the network and return the HttpURLConnection
	 * @param request
	 * @param config
	 * @return an {@link HttpURLConnection} with the network response
	 * @throws HttpException
	 */
	public static HttpURLConnection getQueryResponse(HttpRequest request) throws HttpException {
		final URL url;
		try {
			url = request.getURL();
		} catch (MalformedURLException e) {
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Malformed URL on:"+request);
			builder.setCause(e);
			throw builder.build();
		}

		HttpURLConnection connection = null;
		try {
			connection = openURL(url);
			// TODO handle transparent gzip when okclient is not used request.addHeader("Accept-Encoding", "gzip");

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

			request.settleHttpHeaders();
			request.setConnectionProperties(connection);

			final LoggerTagged logger = request.getLogger(); 
			if (null != logger) {
				logger.d(connection.getRequestMethod() + ' ' + request.getUri());
				for (Entry<String, List<String>> header : connection.getRequestProperties().entrySet()) {
					logger.d(header.getKey()+": "+header.getValue());
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
				logger.d(connection.getResponseMessage());
				for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
					logger.d(header.getKey()+": "+header.getValue());
				}
			}
			
		} catch (SecurityException e) {
			LogManager.getLogger().w("fail for "+request+' '+e);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Security error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			throw builder.build();

		} catch (SocketTimeoutException e) {
			LogManager.getLogger().i("timeout for "+request);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Timeout error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_TIMEOUT);
			throw builder.build();

		} catch (IOException e) {
			LogManager.getLogger().i("fail for "+request);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("IO error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			throw builder.build();

		} finally {
			try {
				request.setResponse(connection);
			} catch (IllegalStateException e) {
				// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/689
				LogManager.getLogger().d("connection closed ? for "+request+' '+e);
				HttpException.Builder builder = request.newException();
				builder.setErrorMessage("Connection closed "+e.getMessage());
				builder.setCause(e);
				builder.setErrorCode(HttpException.ERROR_NETWORK);
				throw builder.build();
			} catch (ArrayIndexOutOfBoundsException e) {
				// okhttp 1.5.3 issue https://github.com/square/okhttp/issues/658
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
		HttpURLConnection resp = getQueryResponse(request);

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

				if (resp.getResponseMessage()==null) {
					StringBuilder sb = contentLength!=0 ? new StringBuilder(contentLength) : new StringBuilder();
					BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 1250);
					for (String line = reader.readLine(); line!=null; line = reader.readLine())
						sb.append(line);
					reader.close();

					HttpException.Builder builder = request.newException();
					builder.setErrorMessage(sb.length()==0 ? "empty response" : sb.toString());
					builder.setErrorCode(HttpException.ERROR_HTTP);
					throw builder.build();
				}

				final String expectedMimeType = resp.getRequestProperty("Accept");
				if (!TextUtils.isEmpty(expectedMimeType)) {
					// test if it's the right MIME type or throw an exception that can be caught to use the bad data
					String contentType = (resp.getInputStream()==null || resp.getContentType()==null) ? null : resp.getContentType();
					if (contentType!=null && !contentType.startsWith(expectedMimeType)) {
						StringBuilder sb = contentLength!=0 ? new StringBuilder(contentLength) : new StringBuilder();
						BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 1250);
						for (String line = reader.readLine(); line!=null; line = reader.readLine())
							sb.append(line);
						reader.close();

						HttpException.Builder builder = request.newException();
						builder.setErrorMessage("Expected '"+expectedMimeType+"' got '"+contentType+"' - "+sb.toString());
						builder.setErrorCode(HttpException.ERROR_HTTP_MIME);
						throw builder.build();
					}
				}

				if (resp.getResponseCode() < 200 || resp.getResponseCode() >= 300) {
					HttpException.Builder builder = request.newExceptionFromResponse();
					builder.setErrorCode(HttpException.ERROR_HTTP);
					throw builder.build();
				}

			} catch (FileNotFoundException e) {
				LogManager.getLogger().i("fail for "+request);
				HttpException.Builder builder = request.newExceptionFromResponse();
				builder.setCause(e);
				throw builder.build();

			} catch (SocketTimeoutException e) {
				HttpException.Builder builder = request.newException();
				builder.setErrorMessage("timeout");
				builder.setCause(e);
				builder.setErrorCode(HttpException.ERROR_TIMEOUT);
				throw builder.build();

			} catch (IOException e) {
				LogManager.getLogger().i("fail for "+request);
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
	 * @param parser The {@link InputStreamParser parser} used to transform the input stream into the desired type
	 * @return The parsed object or null
	 * @throws HttpException
	 */
	public static <T> T parseRequest(HttpRequest request, InputStreamParser<T> parser) throws HttpException {
		if (null==parser) throw new NullPointerException();
		InputStream is = getInputStream(request);
		if (null==is)
			return null;

		try {
			return parser.parseInputStream(is, request);

		} catch (SocketTimeoutException e) {
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("timeout");
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_TIMEOUT);
			throw builder.build();

		} catch (ArrayIndexOutOfBoundsException e) {
			// okhttp 1.5.3 issue https://github.com/square/okhttp/issues/658
			LogManager.getLogger().d("connection closed ? for "+request+' '+e);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Connection closed "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			throw builder.build();

		} catch (IOException e) {
			LogManager.getLogger().i("fail for "+request);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("IO error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			throw builder.build();

		} finally {
			try {
				is.close();
			} catch (NullPointerException ignored) {
				// okhttp 2.0 bug https://github.com/square/okhttp/issues/690
			} catch (IOException ignored) {
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
