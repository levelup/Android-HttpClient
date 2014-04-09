package com.levelup.http.async;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.levelup.http.HttpRequest;
import com.levelup.http.InputStreamParser;

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

	public DownloadTask(final HttpRequest request, final InputStreamParser<T> parser, AsyncHttpCallback<T> callback) {
		this(new HttpCallable<T>(request, parser), callback);
	}

	public DownloadTask(Callable<T> callable, AsyncHttpCallback<T> callback) {
		super(callable);
		this.callback = callback;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		boolean result = super.cancel(mayInterruptIfRunning);
		if (callback instanceof Closeable) {
			try {
				((Closeable) callback).close();
			} catch (IOException ignored) {
			}
		}
		return result;
	}

	/**
	 * Method called in the UI thread after the job has finished running
	 */
	protected void onDownloadDone() {
		if (null!=callback)
			try {
				T result = get();
				if (!isCancelled()) {
					callback.onHttpSuccess(result);
				}
			} catch (CancellationException e) {
				// do nothing, the job was canceled
			} catch (InterruptedException e) {
				// do nothing, the job was canceled
			} catch (ExecutionException e) {
				callback.onHttpFailed(e.getCause());
			} finally {
				callback.onHttpFinished();
			}
	}

	@Override
	public void run() {
		if (null!=callback)
			uiHandler.post(new Runnable() {
				@Override
				public void run() {
					callback.onHttpStarted();
				}
			});

		super.run();
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