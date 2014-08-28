package com.levelup.http.ion;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.apache.http.protocol.HTTP;

import android.net.Uri;

import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.TransformFuture;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.ConnectionClosedException;
import com.koushikdutta.async.http.libcore.RawHeaders;
import com.koushikdutta.async.parser.AsyncParser;
import com.koushikdutta.async.parser.JSONArrayParser;
import com.koushikdutta.async.parser.JSONObjectParser;
import com.koushikdutta.async.parser.StringParser;
import com.koushikdutta.ion.InputStreamParser;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.builder.LoadBuilder;
import com.koushikdutta.ion.future.ResponseFuture;
import com.koushikdutta.ion.loader.AsyncHttpRequestFactory;
import com.levelup.http.BaseHttpRequest;
import com.levelup.http.DataErrorException;
import com.levelup.http.HttpBodyJSON;
import com.levelup.http.HttpBodyMultiPart;
import com.levelup.http.HttpBodyParameters;
import com.levelup.http.HttpBodyString;
import com.levelup.http.HttpBodyUrlEncoded;
import com.levelup.http.HttpException;
import com.levelup.http.HttpExceptionCreator;
import com.levelup.http.HttpRequest;
import com.levelup.http.HttpResponse;
import com.levelup.http.ParserException;
import com.levelup.http.ResponseHandler;
import com.levelup.http.UploadProgressListener;
import com.levelup.http.internal.BaseHttpEngine;
import com.levelup.http.ion.internal.HttpLoaderWithError;
import com.levelup.http.ion.internal.IonAsyncHttpRequest;
import com.levelup.http.ion.internal.IonBody;
import com.levelup.http.ion.internal.IonHttpBodyJSON;
import com.levelup.http.ion.internal.IonHttpBodyMultiPart;
import com.levelup.http.ion.internal.IonHttpBodyString;
import com.levelup.http.ion.internal.IonHttpBodyUrlEncoded;
import com.levelup.http.parser.ErrorHandlerParser;
import com.levelup.http.parser.Utils;
import com.levelup.http.parser.XferTransform;
import com.levelup.http.parser.XferTransformChain;
import com.levelup.http.parser.XferTransformInputStreamString;
import com.levelup.http.parser.XferTransformResponseInputStream;
import com.levelup.http.parser.XferTransformStringJSONArray;
import com.levelup.http.parser.XferTransformStringJSONObject;

/**
 * Basic HTTP request to be passed to {@link com.levelup.http.HttpClient}
 *
 * @param <T> type of the data read from the HTTP response
 * @see com.levelup.http.HttpRequestGet for a more simple API
 * @see com.levelup.http.HttpRequestPost for a more simple POST API
 */
public class HttpEngineIon<T> extends BaseHttpEngine<T, HttpResponseIon<T>> {
	public final Builders.Any.B requestBuilder;
	private static Ion ion;
	private RawHeaders headers;
	private InputStream inputStream;

	public HttpEngineIon(BaseHttpRequest.AbstractBuilder<T, ?> builder) {
		super(wrapBuilderBodyParams(builder));

		if (builder.getContext() == null) {
			throw new NullPointerException("Ion HTTP request with no Context, try calling HttpClient.setup() first or a constructor with a Context");
		}

		synchronized (HttpEngineIon.class) {
			if (ion == null) {
				ion = Ion.getDefault(builder.getContext());
				// until https://github.com/koush/AndroidAsync/issues/210 is fixed
				ion.getConscryptMiddleware().enable(false);
				ion.configure().addLoader(0, new HttpLoaderWithError());
				ion.configure().setAsyncHttpRequestFactory(new AsyncHttpRequestFactory() {
					@Override
					public AsyncHttpRequest createAsyncHttpRequest(Uri uri, String method, RawHeaders headers, Object cookie) {
						return new IonAsyncHttpRequest(uri, method, headers, (HttpEngineIon) cookie);
					}
				});
			}
		}

		final LoadBuilder<Builders.Any.B> ionLoadBuilder = ion.build(builder.getContext());
		this.requestBuilder = ionLoadBuilder.load(getHttpMethod(), getUri().toString());
		requestBuilder.setHttpRequestCookie(this);
	}

	private static <T> BaseHttpRequest.AbstractBuilder<T, ?> wrapBuilderBodyParams(BaseHttpRequest.AbstractBuilder<T, ?> builder) {
		final HttpBodyParameters sourceBody = builder.getBodyParams();
		if (sourceBody instanceof HttpBodyMultiPart)
			builder.setBody(builder.getHttpMethod(), new IonHttpBodyMultiPart((HttpBodyMultiPart) sourceBody));
		else if (sourceBody instanceof HttpBodyJSON)
			builder.setBody(builder.getHttpMethod(), new IonHttpBodyJSON((HttpBodyJSON) sourceBody));
		else if (sourceBody instanceof HttpBodyUrlEncoded)
			builder.setBody(builder.getHttpMethod(), new IonHttpBodyUrlEncoded((HttpBodyUrlEncoded) sourceBody));
		else if (sourceBody instanceof HttpBodyString)
			builder.setBody(builder.getHttpMethod(), new IonHttpBodyString((HttpBodyString) sourceBody));
		else if (sourceBody != null)
			throw new IllegalStateException("Unknown body type "+sourceBody);

		return builder;
	}

	@Override
	public boolean isStreaming() {
		return false;
	}

	@Override
	public void settleHttpHeaders(HttpRequest request) throws HttpException {
		if (!isMethodWithBody(getHttpMethod())) {
			setHeader(HTTP.CONTENT_LEN, "0");
		}

		super.settleHttpHeaders(request);

		for (Entry<String, String> entry : mRequestSetHeaders.entrySet()) {
			requestBuilder.setHeader(entry.getKey(), entry.getValue());
		}
		for (Entry<String, HashSet<String>> entry : mRequestAddHeaders.entrySet()) {
			for (String value : entry.getValue()) {
				requestBuilder.addHeader(entry.getKey(), value);
			}
		}

		if (null!=followRedirect) {
			requestBuilder.followRedirect(followRedirect);
		}

		if (null != getHttpConfig()) {
			int readTimeout = getHttpConfig().getReadTimeout(request);
			if (readTimeout >= 0)
				requestBuilder.setTimeout(readTimeout);
		}
	}

	@Override
	public final void setupBody() {
		if (null == requestBuilder) throw new IllegalStateException("is this a streaming request?");
		if (null != bodyParams) {
			((IonBody) bodyParams).setOutputData(requestBuilder);

			final UploadProgressListener progressListener = getProgressListener();
			if (null != progressListener) {
				requestBuilder.progress(new ProgressCallback() {
					@Override
					public void onProgress(long downloaded, long total) {
						progressListener.onParamUploadProgress(HttpEngineIon.this, null, (int) ((100 * downloaded) / total));
					}
				});
			}
		}
	}

	@Override
	public final void doConnection() throws IOException {
		// do nothing
	}

	@Override
	public InputStream getInputStream(HttpRequest request, ResponseHandler<?> responseHandler) throws HttpException {
		// TODO do we still need this ?
		if (inputStream == null) {
			prepareRequest(request);
			ResponseFuture<InputStream> req = requestBuilder.asInputStream();
			Future<Response<InputStream>> withResponse = req.withResponse();
			inputStream = getServerResponse(withResponse, request, responseHandler, XferTransformResponseInputStream.INSTANCE);
		}
		return inputStream;
	}

	public RawHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(RawHeaders headers) {
		this.headers = headers;
	}

	@Override
	public <P> P parseRequest(ResponseHandler<P> responseHandler, HttpRequest request) throws HttpException {
		XferTransform<HttpResponse, ?> errorParser = ((ErrorHandlerParser) responseHandler.errorHandler).errorDataParser;
		XferTransform<HttpResponse, ?> commonTransforms = Utils.getCommonXferTransform(responseHandler.contentParser, errorParser);
		AsyncParser<Object> parser = getXferTransformParser(commonTransforms);
		prepareRequest(request);
		ResponseFuture<Object> req = requestBuilder.as(parser);
		Future<Response<Object>> withResponse = req.withResponse();
		P parsedResult = (P) getServerResponse(withResponse, request, responseHandler, commonTransforms);
		return parsedResult;
	}

	private static boolean isHttpError(HttpResponseIon<?> httpResponse) {
		return httpResponse.getResponseCode() < 200 || httpResponse.getResponseCode() >= 400;
	}

	private <P> P getServerResponse(Future<Response<P>> req, HttpRequest request, ResponseHandler<?> responseHandler, XferTransform<HttpResponse,?> commonTransforms) throws HttpException {
		try {
			Response<P> response = req.get();
			HttpResponseIon ionResponse = new HttpResponseIon(response);
			setRequestResponse(request, ionResponse);

			Exception e = ionResponse.getException();
			if (null != e) {
				throw exceptionToHttpException(request, e).build();
			}

			Object data = response.getResult();
			try {
				if (isHttpError(ionResponse)) {
					DataErrorException exceptionWithData = null;

					if (responseHandler.errorHandler instanceof ErrorHandlerParser) {
						ErrorHandlerParser errorHandler = (ErrorHandlerParser) responseHandler.errorHandler;
						XferTransform<Object, Object> transformToResult = Utils.skipCommonTransforms(errorHandler.errorDataParser, commonTransforms);
						Object errorData;
						if (null == transformToResult)
							errorData = data;
						else
							errorData = transformToResult.transformData(data, this);
						exceptionWithData = ((ErrorHandlerParser) responseHandler.errorHandler).handleErrorData(errorData, this);
					}

					HttpException.Builder exceptionBuilder = exceptionToHttpException(request, exceptionWithData);
					throw exceptionBuilder.build();
				}

				XferTransform<Object, Object> transformToResult = Utils.skipCommonTransforms(responseHandler.contentParser, commonTransforms);
				if (null == transformToResult)
					return (P) data;

				return (P) transformToResult.transformData(data, this);
			} catch (ParserException ee) {
				throw exceptionToHttpException(request, ee).build();

			} catch (IOException ee) {
				throw exceptionToHttpException(request, ee).build();
			}

		} catch (InterruptedException e) {
			throw exceptionToHttpException(request, e).build();

		} catch (ExecutionException e) {
			throw exceptionToHttpException(request, e).build();

		}
	}

	@Override
	protected HttpException.Builder exceptionToHttpException(HttpExceptionCreator request, Exception e) throws HttpException {
		if (e instanceof ConnectionClosedException && e.getCause() instanceof Exception) {
			return exceptionToHttpException(request, (Exception) e.getCause());
		}

		return super.exceptionToHttpException(request, e);
	}

	private static final AsyncParser<InputStream> INPUT_STREAM_ASYNC_PARSER = new InputStreamParser();
	private static final AsyncParser<String> STRING_ASYNC_PARSER = new StringParser();
	private static final AsyncParser<?> JSON_OBJECT_ASYNC_PARSER = new JSONObjectParser();
	private static final AsyncParser<?> JSON_ARRAY_ASYNC_PARSER = new JSONArrayParser();

	private <P> AsyncParser<P> getXferTransformParser(XferTransform<HttpResponse, ?> transform) {
		if (transform == XferTransformResponseInputStream.INSTANCE) {
			return (AsyncParser<P>) INPUT_STREAM_ASYNC_PARSER;
		}

		if (transform instanceof XferTransformChain) {
			final XferTransformChain chain = (XferTransformChain) transform;
			if (chain.transforms.length != 0) {
				if (chain.transforms[0] == XferTransformResponseInputStream.INSTANCE) {
					if (chain.transforms.length == 1) {
						return (AsyncParser<P>) INPUT_STREAM_ASYNC_PARSER;
					}

					if (chain.transforms[1] == XferTransformInputStreamString.INSTANCE) {
						if (chain.transforms.length == 2) {
							return (AsyncParser<P>) STRING_ASYNC_PARSER;
						}

						if (chain.transforms[2] == XferTransformStringJSONObject.INSTANCE) {
							if (chain.transforms.length == 3) {
								return (AsyncParser<P>) JSON_OBJECT_ASYNC_PARSER;
							}
						}

						if (chain.transforms[2] == XferTransformStringJSONArray.INSTANCE) {
							if (chain.transforms.length == 3) {
								return (AsyncParser<P>) JSON_ARRAY_ASYNC_PARSER;
							}
						}
					}

					return new AsyncParser<P>() {
						@Override
						public Future<P> parse(DataEmitter emitter) {
							Future<InputStream> inputStreamFuture = INPUT_STREAM_ASYNC_PARSER.parse(emitter);
							return inputStreamFuture.then(new TransformFuture<P, InputStream>() {
								@Override
								protected void transform(InputStream result) throws Exception {
									setComplete((P) chain.skipFirstTransform().transformData(result, HttpEngineIon.this));
								}
							});
						}

						@Override
						public void write(DataSink sink, P value, CompletedCallback completed) {
						}
					};
				}
			}
		}

		throw new IllegalStateException();
	}

	/**
	 * See if we can find common ground to parse the data and the error data inside Ion
	 * @param responseHandler
	 * @return whether Ion will be able to parse the data and the error in its processing thread
	 */
	public static boolean errorCompatibleWithData(ResponseHandler<?> responseHandler) {
		if (!(responseHandler.errorHandler instanceof ErrorHandlerParser)) {
			// not possible to handle the error data with the data coming out of the data parser
			return false;
		}

		ErrorHandlerParser errorHandlerParser = (ErrorHandlerParser) responseHandler.errorHandler;
		return Utils.getCommonXferTransform(responseHandler.contentParser, errorHandlerParser.errorDataParser) != null;
	}
}
