package co.tophe.async;

/**
 * Base class when you want to define an {@link co.tophe.async.AsyncCallback}, does nothing by default.
 * @param <T> the type of data returned by the Async task.
 */
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
