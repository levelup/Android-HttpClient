package com.levelup.http.async;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import android.os.Handler;
import android.os.Looper;

import com.levelup.http.HttpRequest;
import com.levelup.http.HttpResponseHandler;

/**
 * Future task that will be used to do the network download in the background, the result/error will be sent to the {@code AsyncHttpCallback} in the UI thread
 * @author Steve Lhomme
 *
 * @param <T>
 */
public class NetworkTask<T> extends FutureTask<T> {
	private final NetworkCallback<T> callback;

	private static final Handler uiHandler = new Handler(Looper.getMainLooper());

	public NetworkTask(final HttpRequest request, final HttpResponseHandler<T> parser, NetworkCallback<T> callback) {
		this(new HttpCallable<T>(request, parser), callback);
	}

	public NetworkTask(Callable<T> callable, NetworkCallback<T> callback) {
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
					callback.onNetworkSuccess(result);
				}
			} catch (CancellationException e) {
				// do nothing, the job was canceled
			} catch (InterruptedException e) {
				// do nothing, the job was canceled
			} catch (ExecutionException e) {
				callback.onNetworkFailed(e.getCause());
			} finally {
				callback.onNetworkFinished(NetworkTask.this);
			}
	}

	@Override
	public void run() {
		if (null!=callback)
			uiHandler.post(new Runnable() {
				@Override
				public void run() {
					callback.onNetworkStarted(NetworkTask.this);
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