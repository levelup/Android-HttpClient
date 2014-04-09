package com.levelup.http.async;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.levelup.http.HttpClient;
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
	private final HttpRequest request;

	private static final Handler uiHandler = new Handler(Looper.getMainLooper());

	public DownloadTask(final HttpRequest request, final InputStreamParser<T> parser, AsyncHttpCallback<T> callback) {
		this(new Callable<T>() {
			@Override
			public T call() throws Exception {
				return HttpClient.parseRequest(request, parser);
			}
		}, request, callback);
	}
	
	public DownloadTask(Callable<T> callable, HttpRequest request, AsyncHttpCallback<T> callback) {
		super(callable);
		this.callback = callback;
		this.request = request;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		boolean result = super.cancel(mayInterruptIfRunning);
		final HttpURLConnection connection = request.getResponse();
		if (null!=connection) {
			connection.disconnect();
		}
		return result;
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