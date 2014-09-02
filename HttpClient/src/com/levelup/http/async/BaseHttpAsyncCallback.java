package com.levelup.http.async;

public class BaseHttpAsyncCallback<T> implements HttpAsyncCallback<T> {

	@Override
	public void onHttpResult(T result) {
	}

	@Override
	public void onHttpFailed(Throwable t) {
	}

	@Override
	public void onHttpTaskStarted(HttpTask<T> task) {
	}

	@Override
	public void onHttpTaskFinished(HttpTask<T> task) {
	}
}
