package com.levelup.http.async;


/**
 * Callback called in the UI thread when an Async download has finished successfully or with an error (not when canceled)
 * @author Steve Lhomme
 *
 * @param <T>
 */
public interface AsyncHttpCallback<T> {
	/**
	 * The resulting data, called in the UI thread
	 * @param response
	 */
	void onHttpSuccess(T response);
	
	/**
	 * Called when an error has occurred during the download
	 * <p>Will not be called if the download has been interrupted
	 * @param t
	 */
	void onHttpFailed(Throwable t);
	
	/**
	 * Called when the HTTP request is about to start being processed
	 * <p>Always balanced with {@link #onHttpFinished()}
	 */
	void onHttpStarted();
	
	/**
	 * Called when the HTTP request has finished processing
	 * <p>Always balanced with {@link #onHttpStarted()}
	 */
	void onHttpFinished();
}