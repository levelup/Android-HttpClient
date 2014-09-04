package com.levelup.http.async;

public class BaseAsyncCallback<T> implements AsyncCallback<T> {

	@Override
	public void onAsyncResult(T result) {
	}

	@Override
	public void onAsyncFailed(Throwable t) {
	}

	@Override
	public void onAsyncTaskStarted(AsyncTask<T> task) {
	}

	@Override
	public void onAsyncTaskFinished(AsyncTask<T> task) {
	}
}
