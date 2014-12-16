package co.tophe.ion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.TransformFuture;
import com.koushikdutta.async.http.ConnectionClosedException;
import com.koushikdutta.async.http.filter.PrematureDataEndException;
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
import co.tophe.AbstractHttpEngine;
import co.tophe.HttpConfig;
import co.tophe.HttpException;
import co.tophe.HttpResponse;
import co.tophe.HttpTimeoutException;
import co.tophe.ServerException;
import co.tophe.UploadProgressListener;
import co.tophe.body.HttpBodyJSON;
import co.tophe.body.HttpBodyMultiPart;
import co.tophe.body.HttpBodyParameters;
import co.tophe.body.HttpBodyString;
import co.tophe.body.HttpBodyUrlEncoded;
import co.tophe.ion.internal.IonBody;
import co.tophe.ion.internal.IonHttpBodyJSON;
import co.tophe.ion.internal.IonHttpBodyMultiPart;
import co.tophe.ion.internal.IonHttpBodyString;
import co.tophe.ion.internal.IonHttpBodyUrlEncoded;
import co.tophe.log.LogManager;
import co.tophe.parser.ParserException;
import co.tophe.parser.Utils;
import co.tophe.parser.XferTransform;
import co.tophe.parser.XferTransformChain;
import co.tophe.parser.XferTransformInputStreamString;
import co.tophe.parser.XferTransformResponseInputStream;
import co.tophe.parser.XferTransformStringJSONArray;
import co.tophe.parser.XferTransformStringJSONObject;

/**
 * Basic HTTP request to be passed to {@link co.tophe.TopheClient}
 *
 * @param <T> type of the data read from the HTTP response
 * @see co.tophe.HttpRequestGet for a more simple API
 * @see co.tophe.HttpRequestPost for a more simple POST API
 */
public class HttpEngineIon<T, SE extends ServerException> extends AbstractHttpEngine<T, HttpResponseIon<T>, SE> {
	public final Builders.Any.B requestBuilder;
	private static final String ENGINE_SIGNATURE = "Ion-1.4.1+AndroidAsync-1.4.1"; // TODO do not hardcode this

	protected HttpEngineIon(Builder<T,SE> builder, Ion ion) {
		super(builder);

		final LoadBuilder<Builders.Any.B> ionLoadBuilder = ion.build(ion.getContext());
		this.requestBuilder = ionLoadBuilder.load(request.getHttpMethod(), null==request.getUri() ? null : request.getUri().toString());

		final HttpBodyParameters sourceBody = request.getBodyParameters();
		final IonBody ionBody;
		if (sourceBody instanceof HttpBodyMultiPart)
			ionBody = new IonHttpBodyMultiPart((HttpBodyMultiPart) sourceBody);
		else if (sourceBody instanceof HttpBodyJSON)
			ionBody = new IonHttpBodyJSON((HttpBodyJSON) sourceBody);
		else if (sourceBody instanceof HttpBodyUrlEncoded)
			ionBody = new IonHttpBodyUrlEncoded((HttpBodyUrlEncoded) sourceBody);
		else if (sourceBody instanceof HttpBodyString)
			ionBody = new IonHttpBodyString((HttpBodyString) sourceBody);
		else if (sourceBody != null)
			throw new IllegalStateException("Unknown body type "+sourceBody);
		else
			ionBody = null;

		if (null != ionBody) {
			ionBody.setOutputData(requestBuilder);

			final UploadProgressListener progressListener = request.getProgressListener();
			if (null != progressListener) {
				requestBuilder.progress(new ProgressCallback() {
					@Override
					public void onProgress(long downloaded, long total) {
						progressListener.onParamUploadProgress(request, null, (int) ((100 * downloaded) / total));
					}
				});
			}
		}
	}

	@Override
	protected String getEngineSignature() {
		return ENGINE_SIGNATURE;
	}

	@Override
	protected void setContentLength(long contentLength) {
		if (0L == contentLength)
			super.setContentLength(contentLength);
	}

	@Override
	public void setHeadersAndConfig() {
		if (request.getBodyParameters() instanceof HttpBodyMultiPart) {
			setHeader(HTTP.CONTENT_TYPE, null);
		}

		for (Entry<String, String> entry : requestHeaders.entrySet()) {
			requestBuilder.setHeader(entry.getKey(), entry.getValue());
		}

		if (null != responseHandler.followsRedirect()) {
			requestBuilder.followRedirect(responseHandler.followsRedirect());
		}

		HttpConfig httpConfig = request.getHttpConfig();
		if (null != httpConfig) {
			int readTimeout = httpConfig.getReadTimeout(request);
			if (readTimeout >= 0)
				requestBuilder.setTimeout(readTimeout);
		}
	}

	@Override
	protected HttpResponseIon<T> queryResponse() throws HttpException, SE {
		XferTransform<HttpResponse, SE> errorParser = responseHandler.errorParser;
		XferTransform<HttpResponse, ?> commonTransforms = Utils.getCommonXferTransform(responseHandler.contentParser, errorParser, true);
		AsyncParser<Object> parser = getXferTransformParser(commonTransforms);
		ResponseFuture<Object> req = requestBuilder.as(parser);
		Future<Response<Object>> withResponse = req.withResponse();
		try {
			Response<Object> response = withResponse.get();
			HttpResponseIon ionResponse = new HttpResponseIon(response, commonTransforms);
			setRequestResponse(ionResponse);

			Exception e = response.getException();
			if (null != e) {
				throw exceptionToHttpException(e).build();
			}

			if (isHttpError(ionResponse)) {
				Object data = response.getResult();
				XferTransform<Object, Object> transformToResult = Utils.skipCommonTransforms(errorParser, commonTransforms);
				SE errorData;
				if (null == transformToResult)
					errorData = (SE) data;
				else
					errorData = (SE) transformToResult.transformData(data, this);
				throw errorData;
			}

			return ionResponse;

		} catch (InterruptedException e) {
			throw exceptionToHttpException(e).build();

		} catch (ExecutionException e) {
			throw exceptionToHttpException(e).build();

		} catch (ParserException e) {
			throw exceptionToHttpException(e).build();

		} catch (IOException e) {
			throw exceptionToHttpException(e).build();

		}
	}

	@Override
	protected T responseToResult(HttpResponseIon<T> response) throws ParserException, IOException {
		Object data = response.getResult();
		XferTransform<Object, Object> transformToResult = Utils.skipCommonTransforms(responseHandler.contentParser, response.getCommonTransform());
		if (null == transformToResult)
			return (T) data;

		return (T) transformToResult.transformData(data, this);
	}

	@Override
	protected HttpException.Builder exceptionToHttpException(Exception e) throws HttpException {
		if (e instanceof IllegalArgumentException && e.getMessage().contains("bytesConsumed is negative")) {
			// TODO only check for a Play Services error when the Exception stack trace comes from "com.google.android.gms.org.conscrypt"
			Context context = IonHttpEngineFactory.getInstance(null).getDefaultIon().getContext();
			PackageManager pm = context.getPackageManager();
			String playServicesVersion = "<unknown>";
			try {
				PackageInfo pI = pm.getPackageInfo("com.google.android.gms", 0);
				if (pI != null) {
					playServicesVersion = String.valueOf(pI.versionCode) + '-' + pI.versionName;
				}
			} catch (PackageManager.NameNotFoundException ignored) {
			}
			LogManager.getLogger().e("Issue #99698 detected on PS:"+playServicesVersion, e);
		}

		if (e instanceof ConnectionClosedException && e.getCause() instanceof Exception) {
			return exceptionToHttpException((Exception) e.getCause());
		}

		if (e instanceof PrematureDataEndException) {
			LogManager.getLogger().d("timeout for "+request);
			HttpTimeoutException.Builder builder = new HttpTimeoutException.Builder(request, httpResponse);
			builder.setErrorMessage("Timeout error " + e.getMessage());
			builder.setCause(e);
			return builder;
		}

		return super.exceptionToHttpException(e);
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
}
