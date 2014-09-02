package com.levelup.http.async;

import java.util.concurrent.Callable;

/**
 * Singleton default {@link HttpTaskFactory} for type {@code T}
 *
 * @param <T>
 */
public class BaseHttpTaskFactory<T> implements HttpTaskFactory<T> {
	@SuppressWarnings("rawtypes")
	public static final BaseHttpTaskFactory instance = new BaseHttpTaskFactory();
	
	@Override
	public HttpTask<T> createHttpTask(Callable<T> callable, HttpAsyncCallback<T> callback) {
		return new HttpTask<T>(callable, callback);
	}
}