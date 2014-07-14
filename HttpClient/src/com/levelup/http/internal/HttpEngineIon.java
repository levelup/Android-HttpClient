package com.levelup.http.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.http.protocol.HTTP;

import android.net.Uri;

import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.libcore.RawHeaders;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.builder.LoadBuilder;
import com.koushikdutta.ion.loader.AsyncHttpRequestFactory;
import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpBodyMultiPart;
import com.levelup.http.HttpException;
import com.levelup.http.UploadProgressListener;
import com.levelup.http.gson.InputStreamGsonParser;

/**
 * Basic HTTP request to be passed to {@link com.levelup.http.HttpClient}
 *
 * @param <T> type of the data read from the HTTP response
 * @see com.levelup.http.HttpRequestGet for a more simple API
 * @see com.levelup.http.HttpRequestPost for a more simple POST API
 */
public class HttpEngineIon<T> extends BaseHttpEngine<T> {
	public final Builders.Any.B requestBuilder;

	public HttpEngineIon(BaseHttpRequest.AbstractBuilder<T, ?> builder) {
		super(builder);

		if (builder.getContext() == null) {
			throw new NullPointerException("Ion HTTP request with no Context, try calling HttpClient.setup() first or a constructor with a Context");
		}

		final Ion ion;
		if (getInputStreamParser() instanceof InputStreamGsonParser) {
			InputStreamGsonParser gsonParser = (InputStreamGsonParser) getInputStreamParser();
			ion = Ion.getInstance(builder.getContext(), gsonParser.getClass().getName());
			ion.configure().setGson(gsonParser.gson);
		} else {
			ion = Ion.getDefault(builder.getContext());
		}

		ion.configure().setAsyncHttpRequestFactory(new AsyncHttpRequestFactory() {
			@Override
			public AsyncHttpRequest createAsyncHttpRequest(Uri uri, String method, RawHeaders headers) {
				AsyncHttpRequest request = new AsyncHttpRequest(uri, method, headers) {
					@Override
					public void logd(String message) {
						if (getLogger() != null)
							getLogger().d(message);
						else
							super.logd(message);
					}

					@Override
					public void logd(String message, Exception e) {
						if (getLogger() != null)
							getLogger().d(message, e);
						else
							super.logd(message, e);
					}

					@Override
					public void logi(String message) {
						if (getLogger() != null)
							getLogger().i(message);
						else
							super.logi(message);
					}

					@Override
					public void logv(String message) {
						if (getLogger() != null)
							getLogger().v(message);
						else
							super.logv(message);
					}

					@Override
					public void logw(String message) {
						if (getLogger() != null)
							getLogger().w(message);
						else
							super.logw(message);
					}

					@Override
					public void loge(String message) {
						if (getLogger() != null)
							getLogger().e(message);
						else
							super.loge(message);
					}

					@Override
					public void loge(String message, Exception e) {
						if (getLogger() != null)
							getLogger().e(message, e);
						else
							super.loge(message, e);
					}

					@Override
					public void setBody(AsyncHttpRequestBody body) {
						if (body instanceof MultipartFormDataBody) {
							MultipartFormDataBody multipartFormDataBody = (MultipartFormDataBody) body;
							multipartFormDataBody.setBoundary(HttpBodyMultiPart.boundary);
						}

						super.setBody(body);
					}
				};
				return request;
			}
		});

		final LoadBuilder<Builders.Any.B> ionLoadBuilder = ion.build(builder.getContext());
		this.requestBuilder = ionLoadBuilder.load(getHttpMethod(), getUri().toString());
	}

	@Override
	public boolean isStreaming() {
		return false;
	}

	@Override
	public void settleHttpHeaders() throws HttpException {
		if (!isMethodWithBody(getHttpMethod())) {
			setHeader(HTTP.CONTENT_LEN, "0");
		}

		super.settleHttpHeaders();

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
			int readTimeout = getHttpConfig().getReadTimeout(this);
			if (readTimeout >= 0)
				requestBuilder.setTimeout(readTimeout);
		}
	}

	@Override
	public final void setupBody() {
		if (null == requestBuilder) throw new IllegalStateException("is this a streaming request?");
		if (null != bodyParams) {
			bodyParams.setOutputData(requestBuilder);

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
	protected InputStream getParseableErrorStream() throws IOException {
		@SuppressWarnings("unchecked")
		Object result = ((HttpResponseIon<T>) getResponse()).getResult();
		if (result instanceof InputStream) {
			return (InputStream) result;
		}
		if (result == null)
			throw new IOException("error stream not supported");

		return new ByteArrayInputStream(result.toString().getBytes());
	}
}
