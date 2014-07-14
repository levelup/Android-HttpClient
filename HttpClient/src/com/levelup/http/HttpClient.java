package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.gson.JsonParseException;

/**
 * HTTP client that handles {@link HttpRequest} 
 */
public class HttpClient {
	public static final String ACCEPT_ENCODING = "Accept-Encoding";

	private static String userAgent;
	private static CookieManager cookieManager;
	private static Header[] defaultHeaders;
	public static Context defaultContext;

	/**
	 * Setup internal values of the {@link HttpClient} using the provided {@link Context}
	 * <p>The user agent is deduced from the app name of the {@code context} if it's not {@code null}</p>
	 * @param context Used to get a proper User Agent for your app, may be {@code null}
	 */
	public static void setup(Context context) {
		if (null!=context) {
			defaultContext = context;
			PackageManager pM = defaultContext.getPackageManager();
			try {
				PackageInfo pI = pM.getPackageInfo(defaultContext.getPackageName(), 0);
				if (pI != null)
					userAgent = pI.applicationInfo.nonLocalizedLabel + "/" + pI.versionCode;
			} catch (NameNotFoundException ignored) {
			}
		}
	}

	public static String getUserAgent() {
		return userAgent;
	}

	public static void setCookieManager(CookieManager cookieManager) {
		HttpClient.cookieManager = cookieManager;
	}

	public static CookieManager getCookieManager() {
		return cookieManager;
	}

	public static void setDefaultHeaders(Header[] headers) {
		defaultHeaders = headers;
	}

	public static Header[] getDefaultHeaders() {
		return defaultHeaders;
	}

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @return The InputStream corresponding to the data stream, may be null
	 * @throws HttpException
	 */
	public static InputStream getInputStream(HttpRequest request) throws HttpException {
		if (request instanceof BaseHttpRequest) {
			BaseHttpRequest baseHttpRequest = (BaseHttpRequest) request;
			HttpEngine httpEngine = baseHttpRequest.getHttpEngine();
			return httpEngine.getInputStream(request);
		}

		return null;
		/*
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

		return is;*/
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

	public static void forwardResponseException(HttpRequest request, Exception e) throws HttpException {
		if (e instanceof InterruptedException) {
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("interrupted");
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_HTTP);
			throw builder.build();
		}

		if (e instanceof ExecutionException) {
			if (e.getCause() instanceof Exception)
				forwardResponseException(request, (Exception) e.getCause());
			else {
				HttpException.Builder builder = request.newException();
				builder.setErrorMessage("execution error");
				builder.setCause(e.getCause());
				builder.setErrorCode(HttpException.ERROR_HTTP);
				throw builder.build();
			}
		}

		if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
			LogManager.getLogger().d("timeout for "+request);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Timeout error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_TIMEOUT);
			throw builder.build();
		}

		if (e instanceof ProtocolException) {
			LogManager.getLogger().d("bad method for " + request + ' ' + e.getMessage());
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Method error " + e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_HTTP);
			throw builder.build();
		}

		if (e instanceof IOException) {
			LogManager.getLogger().d("i/o error for " + request + ' ' + e.getMessage());
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("IO error " + e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			throw builder.build();
		}

		if (e instanceof ParserException) {
			LogManager.getLogger().i("incorrect data for " + request);
			if (e.getCause() instanceof HttpException)
				throw (HttpException) e.getCause();

			HttpException.Builder builder = request.newException();
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_PARSER);
			throw builder.build();
		}

		if (e instanceof JsonParseException) {
			LogManager.getLogger().i("incorrect data for " + request);
			HttpException.Builder builder = request.newException();
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_PARSER);
			throw builder.build();
		}

		if (e instanceof SecurityException) {
			LogManager.getLogger().w("security error for " + request + ' ' + e);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Security error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			throw builder.build();
		}

		LogManager.getLogger().w("unknown error for " + request + ' ' + e);
		HttpException.Builder builder = request.newException();
		builder.setCause(e);
		builder.setErrorCode(HttpException.ERROR_HTTP);
		throw builder.build();
	}

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @param parser The {@link InputStreamParser parser} used to transform the input stream into the desired type. May be {@code null}
	 * @return The parsed object or null
	 * @throws HttpException
	 */
	public static <T> T parseRequest(final HttpRequest request, InputStreamParser<T> parser) throws HttpException {
		if (request instanceof BaseHttpRequest) {
			BaseHttpRequest baseHttpRequest = (BaseHttpRequest) request;
			HttpEngine httpEngine = baseHttpRequest.getHttpEngine();
			try {
				return (T) httpEngine.parseRequest(parser, request);

			} catch (HttpException forward) {
				throw forward;

			} catch (Exception e) {
				forwardResponseException(request, e);
			}
		}

		InputStream is = getInputStream(request);
		if (null != is)
			try {
				if (null != parser)
					return parser.parseInputStream(is, request);

			} catch (IOException e) {
				forwardResponseException(request, e);

			} catch (ParserException e) {
				forwardResponseException(request, e);

			} finally {
				try {
					is.close();
				} catch (NullPointerException ignored) {
					// okhttp 2.0 bug https://github.com/square/okhttp/issues/690
				} catch (IOException ignored) {
				}
			}

		return null;
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
