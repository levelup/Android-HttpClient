package com.levelup.http.async;

/**
 * Created by robUx4 on 03/09/2014.
 */
public class DelegateHttpAsyncCallback<T> implements HttpAsyncCallback<T> {

	private final HttpAsyncCallback<T> delegate;

	public DelegateHttpAsyncCallback(HttpAsyncCallback<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void onHttpResult(T result) {
		if (null != delegate)
			delegate.onHttpResult(result);
	}

	@Override
	public void onHttpFailed(Throwable t) {
		if (null != delegate)
			delegate.onHttpFailed(t);
	}

	@Override
	public void onHttpTaskStarted(HttpTask<T> task) {
		if (null != delegate)
			delegate.onHttpTaskStarted(task);
	}

	@Override
	public void onHttpTaskFinished(HttpTask<T> task) {
		if (null != delegate)
			delegate.onHttpTaskFinished(task);
	}
}
