package com.levelup.http.async;


/**
 * Callback called in the UI thread when an Async download has finished successfully or with an error (not when canceled)
 * @author Steve Lhomme
 *
 * @param <T>
 */
public interface NetworkCallback<T> {
	/**
	 * The resulting data, called in the UI thread
	 * @param result The parsed response after execution
	 */
	void onNetworkSuccess(T result);
	
	/**
	 * Called when an error has occurred during the download
	 * <p>Will not be called if the download has been interrupted
	 * @param t The {@link Throwable} that caused the execution to fail
	 */
	void onNetworkFailed(Throwable t);
	
	/**
	 * Called when the HTTP request is about to start being processed
	 * <p>Always balanced with {@link #onNetworkFinished(NetworkTask)}
	 * @param task The {@link NetworkTask} that has started
	 */
	void onNetworkStarted(NetworkTask<T> task);
	
	/**
	 * Called when the HTTP request has finished processing
	 * <p>Always balanced with {@link #onNetworkStarted(NetworkTask)}
	 * @param task The {@link NetworkTask} that has stopped
	 */
	void onNetworkFinished(NetworkTask<T> task);
}
