package com.levelup.http.ion.internal;

import android.net.Uri;

import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.libcore.RawHeaders;
import com.levelup.http.HttpBodyMultiPart;
import com.levelup.http.LoggerTagged;
import com.levelup.http.ion.HttpEngineIon;

/**
 * Created by robUx4 on 19/08/2014.
 */
public class IonAsyncHttpRequest extends AsyncHttpRequest {
	final HttpEngineIon engineIon;

	public IonAsyncHttpRequest(Uri uri, String method, RawHeaders headers, HttpEngineIon engineIon) {
		super(uri, method, headers);
		this.engineIon = engineIon;
	}

	private LoggerTagged getLogger() {
		return engineIon.getLogger();
	}

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
}
