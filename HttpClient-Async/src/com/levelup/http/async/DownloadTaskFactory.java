package com.levelup.http.async;

import java.util.concurrent.Callable;

/**
 * Factory interface to create a {@link DownloadTask}
 * <p>By default {@link BaseDownloadTaskFactory#instance} is used by {@link AsyncHttpClient} 
 * @author Steve Lhomme
 *
 * @param <T>
 */
public interface DownloadTaskFactory<T> {
	/**
	 * Create a new {@link DownloadTask} to process the network {@link Callable} and call the {@code callback} in the UI thread when done
	 * @param callable The callable to execute in the network thread
	 * @param callback The callback that will be called in the UI thread after the job is done or on error
	 * @return The {@link DownloadTask} to run
	 */
	DownloadTask<T> createDownloadTask(Callable<T> callable, AsyncHttpCallback<T> callback);
}