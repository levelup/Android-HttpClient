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
	 * Singleton default {@link DownloadTaskFactory} for type {@code T}
	 *
	 * @param <T>
	 */
	public static class BaseDownloadTaskFactory<T> implements DownloadTaskFactory<T> {
		@SuppressWarnings("rawtypes")
		public static final BaseDownloadTaskFactory instance = new BaseDownloadTaskFactory();
	
		@Override
		public DownloadTask<T> createDownloadTask(Callable<T> netReq, AsyncHttpCallback<T> httpListener) {
			return new DownloadTask<T>(netReq, httpListener);
		}
	}

	/**
	 * Create a new {@link DownloadTask} to process the network {@link Callable} and call the {@code callback} in the UI thread when done
	 * @param netReq The code that will be executed in the network thread 
	 * @param callback The callback that will be called in the UI thread after the job is done or on error
	 * @return The {@link DownloadTask} to run
	 */
	DownloadTask<T> createDownloadTask(Callable<T> netReq, AsyncHttpCallback<T> callback);
}