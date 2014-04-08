package com.levelup.http.async;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import android.os.Handler;
import android.os.Looper;

/**
 * Future task that will be used to do the network download in the background, the result/error will be sent to the {@code AsyncHttpCallback} in the UI thread
 * @author Steve Lhomme
 *
 * @param <T>
 */
public class DownloadTask<T> extends FutureTask<T> {
	private final AsyncHttpCallback<T> callback;

	private static final Handler uiHandler = new Handler(Looper.getMainLooper());

	public DownloadTask(Callable<T> callable, AsyncHttpCallback<T> callback) {
		super(callable);
		this.callback = callback;
	}

	/**
	 * Method called in the UI thread after the job has finished running
	 */
	protected void onDownloadDone() {
		try {
			T result = get();
			if (null!=callback && !isCancelled()) {
				callback.onHttpSuccess(result);
			}
		} catch (CancellationException e) {
			// do nothing, the job was canceled
		} catch (InterruptedException e) {
			// do nothing, the job was canceled
		} catch (ExecutionException e) {
			if (null!=callback)
				callback.onHttpError(e.getCause());
		}
	}

	protected final void done() {
		super.done();

		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				onDownloadDone();
			}
		});
	}
}