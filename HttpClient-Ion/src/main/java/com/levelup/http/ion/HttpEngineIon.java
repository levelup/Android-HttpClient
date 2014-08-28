package com.levelup.http.ion;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.apache.http.protocol.HTTP;

import android.net.Uri;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.TransformFuture;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.ConnectionClosedException;
import com.koushikdutta.async.http.libcore.RawHeaders;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.builder.LoadBuilder;
import com.koushikdutta.ion.future.ResponseFuture;
import com.koushikdutta.ion.gson.GsonSerializer;
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
import com.levelup.http.LogManager;
import com.levelup.http.ParserException;
import com.levelup.http.ResponseHandler;
import com.levelup.http.UploadProgressListener;
import com.levelup.http.gson.XferTransformViaGson;
import com.levelup.http.internal.BaseHttpEngine;
import com.levelup.http.ion.internal.HttpLoaderWithError;
import com.levelup.http.ion.internal.IonAsyncHttpRequest;
import com.levelup.http.ion.internal.IonBody;
import com.levelup.http.ion.internal.IonHttpBodyJSON;
import com.levelup.http.ion.internal.IonHttpBodyMultiPart;
import com.levelup.http.ion.internal.IonHttpBodyString;
import com.levelup.http.ion.internal.IonHttpBodyUrlEncoded;
import com.levelup.http.parser.ErrorHandlerParser;
import com.levelup.http.parser.XferTransform;
import com.levelup.http.parser.XferTransformChain;
import com.levelup.http.parser.XferTransformResponseInputStream;

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
		requestBuilder.setHttpRequestCookie(HttpEngineIon.this);
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

	private static GsonSerializer getGsonSerializer(XferTransform<HttpResponse, ?> xferTransform) {
		if (xferTransform instanceof XferTransformChain) {
			XferTransformChain contentParser = (XferTransformChain) xferTransform;
			if (contentParser.transforms.length > 1 && contentParser.transforms[0] == XferTransformResponseInputStream.INSTANCE && contentParser.transforms[1] instanceof XferTransformViaGson) {
				XferTransformViaGson gsonParser = (XferTransformViaGson) contentParser.transforms[1];
				if (!gsonParser.debugEnabled()) {
					return getGsonSerializer(gsonParser, contentParser.skipFirstTransform().skipFirstTransform());
				}
			}
		}
		return null;
	}

	private static GsonSerializer getGsonSerializer(XferTransformViaGson<?> gsonParser, final XferTransformChain remainingTransforms) {
		if (gsonParser.getGsonOutputTypeToken() != null) {
			TypeToken<?> typeToken = gsonParser.getGsonOutputTypeToken();
			return new GsonSerializer(gsonParser.getGsonHandler(), typeToken) {
				@Override
				public Future parse(DataEmitter emitter) {
					Future<Object> r = (Future<Object>) super.parse(emitter);
					return r.then(new TransformFuture<Object, Object>() {
						@Override
						protected void transform(Object gsonResult) throws Exception {
							setComplete(remainingTransforms.transformData(gsonResult, null));
						}
					});
				}
			};
		}

		if (gsonParser.getGsonOutputType() instanceof Class) {
			Class<?> clazz = (Class<?>) gsonParser.getGsonOutputType();
			return new GsonSerializer(gsonParser.getGsonHandler(), clazz) {
				@Override
				public Future parse(DataEmitter emitter) {
					Future<Object> r = (Future<Object>) super.parse(emitter);
					return r.then(new TransformFuture<Object, Object>() {
						@Override
						protected void transform(Object gsonResult) throws Exception {
							setComplete(remainingTransforms.transformData(gsonResult, null));
						}
					});
				}
			};
		}

		LogManager.getLogger().e("Impossible to use " + gsonParser + " with output+" + gsonParser.getGsonOutputType() + " typeToken:" + gsonParser.getGsonOutputTypeToken());
		return null;
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
		if (inputStream == null) {
			prepareRequest(request);
			ResponseFuture<InputStream> req = requestBuilder.asInputStream();
			Future<Response<InputStream>> withResponse = req.withResponse();
			inputStream = getServerResponse(withResponse, request, responseHandler);
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
		// special case: Gson data handling with HttpRequestIon
		if (null != responseHandler && responseHandler.contentParser instanceof XferTransformChain) {
			GsonSerializer gsonSerializer = getGsonSerializer(responseHandler.contentParser);
			if (null != gsonSerializer) {
				prepareRequest(request);
				ResponseFuture<P> req = requestBuilder.as(gsonSerializer);
				Future<Response<P>> withResponse = req.withResponse();
				return getServerResponse(withResponse, request, responseHandler);
			}
		}

		return super.parseRequest(responseHandler, request);
	}

	private <P> P getServerResponse(Future<Response<P>> req, HttpRequest request, ResponseHandler<?> responseHandler) throws HttpException {
		try {
			Response<P> response = req.get();
			setRequestResponse(request, new HttpResponseIon(response));

			Exception e = getHttpResponse().getException();
			if (null!=e) {
				throw exceptionToHttpException(request, e).build();
			}

			if (null != responseHandler) {
				if (getHttpResponse().getResponseCode() < 200 || getHttpResponse().getResponseCode() >= 400) {
					DataErrorException exceptionWithData = null;

					if (responseHandler.errorHandler instanceof ErrorHandlerParser) {
						ErrorHandlerParser errorHandler = (ErrorHandlerParser) responseHandler.errorHandler;
						GsonSerializer gsonSerializer = getGsonSerializer(errorHandler.errorDataParser);
						if (null != gsonSerializer) {
							ResponseFuture<Object> errorReq = requestBuilder.as(gsonSerializer);
							Future<Response<Object>> withResponse = errorReq.withResponse();
							Object data = getServerResponse(withResponse, request, null);
							exceptionWithData = new DataErrorException(data, null);
						}
					}
					if (null == exceptionWithData) {
						try {
							exceptionWithData = responseHandler.errorHandler.handleError(getHttpResponse(), this, getHttpResponse().getException());

						} catch (ParserException ee) {
							throw exceptionToHttpException(request, ee).build();

						} catch (IOException ee) {
							throw exceptionToHttpException(request, ee).build();
						}
					}

					HttpException.Builder exceptionBuilder = exceptionToHttpException(request, exceptionWithData);
					throw exceptionBuilder.build();
				}
			}

			return (P) response.getResult();

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
}
