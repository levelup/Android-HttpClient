package com.levelup.http.async;

public class BaseAsyncHttpCallback<T> implements AsyncHttpCallback<T> {

	@Override
	public void onHttpSuccess(T response) {
	}

	@Override
	public void onHttpFailed(Throwable t) {
	}

	@Override
	public void onHttpStarted() {
	}

	@Override
	public void onHttpFinished() {
	}
}
