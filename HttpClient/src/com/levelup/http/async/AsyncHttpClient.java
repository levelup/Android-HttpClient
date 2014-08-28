package com.levelup.http.async;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpRequest;
import com.levelup.http.TypedHttpRequest;
import com.levelup.http.ResponseHandler;
import com.levelup.http.parser.ResponseToString;

/**
 * Basic HttpClient to run network queries outside of the UI thread
 * 
 * @author Steve Lhomme
 */
public class AsyncHttpClient {

	private static final int THREAD_POOL_SIZE = 3*Runtime.getRuntime().availableProcessors();
	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>();

	private static final HashMap<String, Future<?>> taggedJobs = new HashMap<String, Future<?>>();

	private static ExecutorService executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS, sPoolWorkQueue);

	@SuppressLint("NewApi")
	private AsyncHttpClient() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
			((ThreadPoolExecutor) executor).allowCoreThreadTimeOut(true);
	}

	/**
	 * Replaces the default {@link ExecutorService} with your own
	 * <p>This should be called before doing any queries
	 * @param newExecutor The {@link ExecutorService} that will run the network queries
	 */
	public static void setExecutorService(ExecutorService newExecutor) {
		executor = newExecutor;
	}

	/**
	 * Get the {@link ExecutorService} used by the Async client.
	 * <p>Can be useful if you want to use it as your network Thread pool 
	 * @return The {@link ExecutorService} used by the Async client.
	 */
	public static ExecutorService getExecutorService() {
		return executor;
	}

	/**
	 * Do a GET request to load a String through this asynchronous client
	 * @param url URL to get the String data from
	 * @param tag String used to match previously running similar jobs to be canceled, null to not cancel anything
	 * @param callback Callback receiving the String or errors (not job canceled) in the UI thread. May be {@code null}
	 */
	public static void getString(String url, String tag, NetworkCallback<String> callback) {
		BaseHttpRequest.Builder<String> reqBuilder = new BaseHttpRequest.Builder<String>(HttpClient.defaultContext);
		reqBuilder.setUrl(url)
				.setResponseParser(ResponseToString.RESPONSE_HANDLER);
		doRequest(reqBuilder.build(), tag, callback);
	}

	/**
	 * Run an {@link TypedHttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * @param request {@link TypedHttpRequest HTTP request} to execute to get the parsed object
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @return A Future<T> representing the download task, if you need to cancel it
	 */
	@SuppressWarnings("unchecked")
	public static <T> Future<T> doRequest(TypedHttpRequest<T> request, NetworkCallback<T> callback) {
		return doRequest(request, callback, BaseNetworkTaskFactory.instance);
	}

	/**
	 * Run an {@link HttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * @param request {@link HttpRequest HTTP request} to execute to get the parsed object
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @param factory Factory used to create the {@link NetworkTask} that will download the data and send the result in the UI thread
	 * @return A Future<T> representing the download task, if you need to cancel it
	 * @see #doRequest(TypedHttpRequest, NetworkCallback)
	 */
	public static <T> Future<T> doRequest(TypedHttpRequest<T> request, NetworkCallback<T> callback, NetworkTaskFactory<T> factory) {
		return doRequest(executor, factory, new HttpCallable<T>(request), callback);
	}

	public static <T> Future<T> doRequest(Executor executor, NetworkTaskFactory<T> factory, Callable<T> callable, NetworkCallback<T> callback) {
		FutureTask<T> task = factory.createNetworkTask(callable, callback);
		executor.execute(task);
		return task;
	}

	/**
	 * Run an {@link HttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * <p>The {@code tag} is used to identify similar queries so previous instances can be canceled so that only the last call gives a result.
	 * @param request {@link HttpRequest HTTP request} to execute to get the parsed object
	 * @param tag String used to match previously running similar jobs to be canceled, null to not cancel anything
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @see #getString(TypedHttpRequest, String, NetworkCallback)
	 */
	public static <T> void doRequest(TypedHttpRequest<T> request, String tag, NetworkCallback<T> callback) {
		if (TextUtils.isEmpty(tag)) {
			doRequest(request, callback);
			return;
		}

		synchronized (taggedJobs) {
			Future<?> oldTask = taggedJobs.get(tag);
			if (null != oldTask) {
				oldTask.cancel(true);
			}

			Future<T> task = doRequest(request, callback, new TaggedStringDownloadFactory<T>(tag));

			taggedJobs.put(tag, task);
		}
	}

	/**
	 * A {@link NetworkTaskFactory} that will remove the tag from the running tasks when done
	 * @param <T>
	 */
	private static class TaggedStringDownloadFactory<T> implements NetworkTaskFactory<T> {
		private final String tag;

		TaggedStringDownloadFactory(String tag) {
			this.tag = tag;
		}

		@Override
		public NetworkTask<T> createNetworkTask(Callable<T> callable, NetworkCallback<T> callback) {
			return new NetworkTask<T>(callable, callback) {
				@Override
				protected void onDownloadDone() {
					try {
						super.onDownloadDone();
					} finally {
						synchronized (taggedJobs) {
							taggedJobs.remove(tag);
						}
					}
				}
			};
		}
	}
}
