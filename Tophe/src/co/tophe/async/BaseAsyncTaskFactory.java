package co.tophe.async;

import java.util.concurrent.Callable;

/**
 * Singleton default {@link AsyncTaskFactory} for type {@code T}
 *
 * @param <T>
 */
public class BaseAsyncTaskFactory<T> implements AsyncTaskFactory<T> {
	@SuppressWarnings("rawtypes")
	public static final BaseAsyncTaskFactory INSTANCE = new BaseAsyncTaskFactory();
	
	@Override
	public AsyncTask<T> createAsyncTask(Callable<T> callable, AsyncCallback<T> callback) {
		return new AsyncTask<T>(callable, callback);
	}
}