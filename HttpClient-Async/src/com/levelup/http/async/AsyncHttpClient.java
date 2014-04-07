package com.levelup.http.async;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Looper;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;
import com.levelup.http.HttpRequest;
import com.levelup.http.HttpRequestGet;

/**
 * Basic HttpClient to run network queries outside of the UI thread
 */
public class AsyncHttpClient {

	private final HashMap<String, Future<?>> taggedJobs = new HashMap<String, Future<?>>();
	private final Handler uiHandler;

	private final static int THREAD_POOL_SIZE = 3*Runtime.getRuntime().availableProcessors();
	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>();
	private ExecutorService executor = new ThreadPoolExecutor(0, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS, sPoolWorkQueue);

	public static AsyncHttpClient instance = new AsyncHttpClient();

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
		synchronized (taggedJobs) {
			if (null!=tag) {
				Future<?> oldJob = taggedJobs.get(tag);
				if (null!=oldJob) {
					oldJob.cancel(true);
				}
			}

			final Future<?> newJob = executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						final String result = HttpClient.getStringResponse(req);
						if (null!=httpListener && !Thread.interrupted())
							uiHandler.post(new Runnable() {
								@Override
								public void run() {
									//if (!newJob.isCancelled())
									httpListener.onHttpSuccess(result);
								}
							});
					} catch (final HttpException e) {
						if (null!=httpListener) 
							uiHandler.post(new Runnable() {
								@Override
								public void run() {
									if (e.getCause() instanceof SocketTimeoutException)
										httpListener.onHttpError(e.getMessage(), AsyncHttpListener.ErrorType.Timeout);
									else
										httpListener.onHttpError(e.getMessage(), AsyncHttpListener.ErrorType.Generic);
								}
							});
					} finally {
						if (null!=tag) {
							synchronized (taggedJobs) {
								taggedJobs.remove(tag);
							}
						}
					}
				}
			});
			if (null!=tag) {
				taggedJobs.put(tag, newJob);
			}
		}
	}
}
