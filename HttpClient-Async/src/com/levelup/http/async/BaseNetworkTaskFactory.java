package com.levelup.http.async;

import java.util.concurrent.Callable;

/**
 * Singleton default {@link NetworkTaskFactory} for type {@code T}
 *
 * @param <T>
 */
public class BaseNetworkTaskFactory<T> implements NetworkTaskFactory<T> {
	@SuppressWarnings("rawtypes")
	public static final BaseNetworkTaskFactory instance = new BaseNetworkTaskFactory();
	
	@Override
	public NetworkTask<T> createNetworkTask(Callable<T> callable, AsyncHttpCallback<T> callback) {
		return new NetworkTask<T>(callable, callback);
	}
}