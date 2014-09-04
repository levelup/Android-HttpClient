package com.levelup.http.async;

import java.util.concurrent.Callable;

/**
 * Factory interface to create a {@link AsyncTask} from a {@link Callable}
 * <p>By default {@link BaseAsyncTaskFactory#instance} is used by {@link AsyncTask.Builder},
 * other factories may be created to do some extra processing in the worker thread.
 * @author Steve Lhomme
 *
 * @param <T>
 */
public interface AsyncTaskFactory<T> {
	/**
	 * Create a new {@link AsyncTask} to execute the {@link Callable} in the network thread and call the {@link AsyncCallback} in the UI thread when done
	 * @param callable The callable to execute in the network thread (usually a {@link com.levelup.http.HttpEngine HttpEngine})
	 * @param callback The callback that will be called in the UI thread after the job is done or on error
	 * @return The {@link AsyncTask} to run
	 */
	AsyncTask<T> createAsyncTask(Callable<T> callable, AsyncCallback<T> callback);
}