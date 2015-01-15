package co.tophe.async;

import java.util.concurrent.Callable;

/**
 * Singleton default {@link AsyncTaskFactory} for type {@code T}
 *
 * @param <T>
 */
public final class BaseAsyncTaskFactory<T> implements AsyncTaskFactory<T> {
	/**
	 * The instance to use to create a basic {@link co.tophe.async.AsyncTask} in the {@link co.tophe.async.AsyncTask.Builder}.
	 */
	@SuppressWarnings("rawtypes")
	public static final BaseAsyncTaskFactory INSTANCE = new BaseAsyncTaskFactory();
	
	@Override
	public final AsyncTask<T> createAsyncTask(Callable<T> callable, AsyncCallback<T> callback) {
		return new AsyncTask<T>(callable, callback);
	}
}