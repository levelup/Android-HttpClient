package co.tophe.async;


import android.support.annotation.Nullable;

/**
 * Callback called in the UI thread when an Async download has finished successfully or with an error (not when canceled)
 * @author Steve Lhomme
 *
 * @param <T>
 */
public interface AsyncCallback<T> {
	/**
	 * The resulting data, called in the UI thread
	 * @param result The parsed response after execution, may be {@code null}
	 *
	 */
	void onAsyncResult(@Nullable T result);
	
	/**
	 * Called when an error has occurred during the download
	 * <p>Will not be called if the download has been interrupted
	 * @param t The {@link Throwable} that caused the execution to fail
	 *
	 */
	void onAsyncFailed(Throwable t);
	
	/**
	 * Called when the asynchronous task is about to start processing
	 * <p>Always balanced with {@link #onAsyncTaskFinished(AsyncTask)}
	 * @param task The {@link AsyncTask} that has started
	 */
	void onAsyncTaskStarted(AsyncTask<T> task);
	
	/**
	 * Called when the asynchronous task has finished processing. You can use {@link AsyncTask#get()} to get the result or an exception if it didn't finish correctly.
	 * <p>Always balanced with {@link #onAsyncTaskStarted(AsyncTask)}
	 * @param task The {@link AsyncTask} that has stopped
	 */
	void onAsyncTaskFinished(AsyncTask<T> task);
}
