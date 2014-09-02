package com.levelup.http.async;

import java.util.concurrent.Callable;

/**
 * Factory interface to create a {@link HttpTask} from a {@link Callable}
 * <p>By default {@link BaseHttpTaskFactory#instance} is used by {@link AsyncHttpClient}
 * @author Steve Lhomme
 *
 * @param <T>
 */
public interface HttpTaskFactory<T> {
	/**
	 * Create a new {@link HttpTask} to execute the {@link Callable} in the network thread and call the {@link HttpAsyncCallback} in the UI thread when done
	 * @param callable The callable to execute in the network thread (usually a {@link com.levelup.http.HttpEngine HttpEngine})
	 * @param callback The callback that will be called in the UI thread after the job is done or on error
	 * @return The {@link HttpTask} to run
	 */
	HttpTask<T> createHttpTask(Callable<T> callable, HttpAsyncCallback<T> callback);
}