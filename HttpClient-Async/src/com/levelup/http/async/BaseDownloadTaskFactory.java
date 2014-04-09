package com.levelup.http.async;

import java.util.concurrent.Callable;

/**
 * Singleton default {@link DownloadTaskFactory} for type {@code T}
 *
 * @param <T>
 */
public class BaseDownloadTaskFactory<T> implements DownloadTaskFactory<T> {
	@SuppressWarnings("rawtypes")
	public static final BaseDownloadTaskFactory instance = new BaseDownloadTaskFactory();
	
	@Override
	public DownloadTask<T> createDownloadTask(Callable<T> callable, AsyncHttpCallback<T> callback) {
		return new DownloadTask<T>(callable, callback);
	}
}