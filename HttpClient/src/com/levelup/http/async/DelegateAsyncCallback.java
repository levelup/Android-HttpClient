package com.levelup.http.async;

/**
 * Created by robUx4 on 03/09/2014.
 */
public class DelegateAsyncCallback<T> implements AsyncCallback<T> {

	private final AsyncCallback<T> delegate;

	public DelegateAsyncCallback(AsyncCallback<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void onAsyncResult(T result) {
		if (null != delegate)
			delegate.onAsyncResult(result);
	}

	@Override
	public void onAsyncFailed(Throwable t) {
		if (null != delegate)
			delegate.onAsyncFailed(t);
	}

	@Override
	public void onAsyncTaskStarted(AsyncTask<T> task) {
		if (null != delegate)
			delegate.onAsyncTaskStarted(task);
	}

	@Override
	public void onAsyncTaskFinished(AsyncTask<T> task) {
		if (null != delegate)
			delegate.onAsyncTaskFinished(task);
	}
}
