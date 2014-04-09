package com.levelup.http.async;

import java.util.concurrent.Callable;

/**
 * Factory interface to create a {@link NetworkTask} from a {@link Callable}
 * <p>By default {@link BaseNetworkTaskFactory#instance} is used by {@link AsyncHttpClient} 
 * @author Steve Lhomme
 *
 * @param <T>
 */
public interface NetworkTaskFactory<T> {
	/**
	 * Create a new {@link NetworkTask} to execute the {@link Callable} in the network thread and call the {@link AsyncHttpCallback} in the UI thread when done
	 * @param callable The callable to execute in the network thread
	 * @param callback The callback that will be called in the UI thread after the job is done or on error
	 * @return The {@link NetworkTask} to run
	 */
	NetworkTask<T> createNetworkTask(Callable<T> callable, AsyncHttpCallback<T> callback);
}