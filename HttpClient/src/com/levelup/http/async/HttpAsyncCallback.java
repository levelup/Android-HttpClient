package com.levelup.http.async;


/**
 * Callback called in the UI thread when an Async download has finished successfully or with an error (not when canceled)
 * @author Steve Lhomme
 *
 * @param <T>
 */
public interface HttpAsyncCallback<T> {
	/**
	 * The resulting data, called in the UI thread
	 * @param result The parsed response after execution, may be {@code null}
	 *
	 */
	void onHttpResult(T result);
	
	/**
	 * Called when an error has occurred during the download
	 * <p>Will not be called if the download has been interrupted
	 * @param t The {@link Throwable} that caused the execution to fail
	 *
	 */
	void onHttpFailed(Throwable t);
	
	/**
	 * Called when the HTTP request is about to start being processed
	 * <p>Always balanced with {@link #onHttpTaskFinished(HttpTask)}
	 * @param task The {@link HttpTask} that has started
	 */
	void onHttpTaskStarted(HttpTask<T> task);
	
	/**
	 * Called when the HTTP request has finished processing. You can use {@link HttpTask#get()} to get the result or an exception if it didn't finish correctly.
	 * <p>Always balanced with {@link #onHttpTaskStarted(HttpTask)}
	 * @param task The {@link HttpTask} that has stopped
	 */
	void onHttpTaskFinished(HttpTask<T> task);
}
