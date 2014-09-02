package com.levelup.http.async;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import android.os.Handler;
import android.os.Looper;

import com.levelup.http.HttpEngine;
import com.levelup.http.TypedHttpRequest;

/**
 * {@link java.util.concurrent.FutureTask FutureTask} that will be used to do the HTTP download in the background,
 * the result/error will be sent to the {@code AsyncHttpCallback} in the UI thread
 * @author Steve Lhomme
 *
 * @param <T>
 */
public class HttpTask<T> extends FutureTask<T> {
	private final HttpAsyncCallback<T> callback;
	private final boolean reportNullResult;

	private static final Handler uiHandler = new Handler(Looper.getMainLooper());

	public HttpTask(TypedHttpRequest<T> request, HttpAsyncCallback<T> callback) {
		this(new HttpEngine.Builder<T>().setTypedRequest(request).build(), callback);
	}

	public HttpTask(Callable<T> callable, HttpAsyncCallback<T> callback) {
		this(callable, callback, true);
	}

	public HttpTask(Callable<T> callable, HttpAsyncCallback<T> callback, boolean reportNullResult) {
		super(callable);
		this.callback = callback;
		this.reportNullResult = reportNullResult;
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
				if (!isCancelled() && (reportNullResult || null != result)) {
					callback.onHttpResult(result);
				}
			} catch (CancellationException e) {
				// do nothing, the job was canceled
			} catch (InterruptedException e) {
				// do nothing, the job was canceled
			} catch (ExecutionException e) {
				callback.onHttpFailed(e.getCause());
			} finally {
				callback.onHttpTaskFinished(this);
			}
	}

	@Override
	public void run() {
		if (null!=callback)
			uiHandler.post(new Runnable() {
				@Override
				public void run() {
					callback.onHttpTaskStarted(HttpTask.this);
				}
			});

		try {
			super.run();
		} finally {
			uiHandler.post(new Runnable() {
				@Override
				public void run() {
					onDownloadDone();
				}
			});
		}
	}
}