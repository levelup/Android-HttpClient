package com.levelup.http.async;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Looper;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpRequest;
import com.levelup.http.HttpRequestGet;

/**
 * Basic HttpClient to run network queries outside of the UI thread
 */
public class AsyncHttpClient {

	private static final int THREAD_POOL_SIZE = 3*Runtime.getRuntime().availableProcessors();
	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>();

	public static AsyncHttpClient instance = new AsyncHttpClient();

	private final HashMap<String, Future<?>> taggedJobs = new HashMap<String, Future<?>>();
	private final Handler uiHandler;

	private ExecutorService executor = new ThreadPoolExecutor(0, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS, sPoolWorkQueue);

	private AsyncHttpClient() {
		this.uiHandler = new Handler(Looper.getMainLooper());
	}

	/**
	 * Replaces the default {@link ExecutorService} with your own
	 * <p>This should be called before doing any queries
	 * @param executor The {@link ExecutorService} that will run the network queries
	 */
	public void setExecutorService(ExecutorService executor) {
		this.executor = executor;
	}

	/**
	 * Get the {@link ExecutorService} used by the Async client.
	 * <p>Can be useful if you want to use it as your network Thread pool 
	 * @return The {@link ExecutorService} used by the Async client.
	 */
	public ExecutorService getExecutorService() {
		return executor;
	}

	/**
	 * Do a GET request to load a String through this asynchronous client
	 * @param url
	 * @param tag String used to match previously running similar jobs to be canceled, null to not cancel anything
	 * @param httpListener listener for data received or errors
	 */
	public void getString(String url, String tag, AsyncHttpListener<String> httpListener) {
		HttpRequestGet req = new HttpRequestGet(url);
		getString(req, tag, httpListener);
	}

	/**
	 * Do an {@link HttpRequest} query to load a String though this asynchronous client
	 * @param req {@link HttpRequest} request to wrap
	 * @param tag String used to match previously running similar jobs to be canceled, null to not cancel anything
	 * @param httpListener listener for data received or errors
	 */
	public void getString(final HttpRequest req, final String tag, final AsyncHttpListener<String> httpListener) {
		Callable<String> netReq = new Callable<String>() {
			@Override
			public String call() throws Exception {
				return HttpClient.getStringResponse(req);
			}
		};
		
		FutureTask<String> task = new FutureTask<String>(netReq) {
			@Override
			protected void done() {
				super.done();

				if (null!=httpListener)
					uiHandler.post(new Runnable() {
						@Override
						public void run() {
							try {
								String result = get();
								httpListener.onHttpSuccess(result);
							} catch (CancellationException e) {
							} catch (InterruptedException e) {
							} catch (ExecutionException e) {
								Throwable t = e.getCause();
								if (t.getCause() instanceof SocketTimeoutException)
									httpListener.onHttpError(t.getMessage(), AsyncHttpListener.ErrorType.Timeout);
								else
									httpListener.onHttpError(t.getMessage(), AsyncHttpListener.ErrorType.Generic);
							} finally {
								if (null!=tag) {
									synchronized (taggedJobs) {
										taggedJobs.remove(tag);
									}
								}
							}
						}
					});
			}
		};

		synchronized (taggedJobs) {
			if (null!=tag) {
				Future<?> oldTask = taggedJobs.get(tag);
				if (null != oldTask) {
					oldTask.cancel(true);
				}
				taggedJobs.put(tag, task);
			}

			executor.execute(task);
		}
	}
}
