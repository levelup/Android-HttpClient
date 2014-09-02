package com.levelup.http.async;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.text.TextUtils;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpEngine;
import com.levelup.http.HttpRequest;
import com.levelup.http.TypedHttpRequest;
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

	private static Executor executor;
	static {
		executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS, sPoolWorkQueue);
		((ThreadPoolExecutor) executor).allowCoreThreadTimeOut(true);
	}

	private AsyncHttpClient() {
	}

	/**
	 * Replaces the default {@link Executor} with your own
	 * <p>This should be called before doing any queries
	 * @param newExecutor The {@link Executor} that will run the network queries
	 */
	public static void setExecutor(Executor newExecutor) {
		executor = newExecutor;
	}

	/**
	 * Get the {@link Executor} used by the Async client.
	 * <p>Can be useful if you want to use it as your network Thread pool 
	 * @return The {@link Executor} used by the Async client.
	 */
	public static Executor getExecutor() {
		return executor;
	}

	/**
	 * Do a GET request to load a String through this asynchronous client
	 * @param url URL to get the String data from
	 * @param tag String used to match previously running similar jobs to be canceled, null to not cancel anything
	 * @param callback Callback receiving the String or errors (not job canceled) in the UI thread. May be {@code null}
	 */
	public static void postStringRequest(String url, String tag, NetworkCallback<String> callback) {
		BaseHttpRequest.Builder<String> reqBuilder = new BaseHttpRequest.Builder<String>();
		reqBuilder.setUrl(url)
				.setResponseParser(ResponseToString.RESPONSE_HANDLER);
		postRequest(reqBuilder.build(), tag, callback);
	}

	/**
	 * Run an {@link TypedHttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * @param request {@link TypedHttpRequest HTTP request} to execute to get the parsed object
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @param <T> Type of the Object generated from the parsed data
	 * @return A Future<T> representing the download task, if you need to cancel it
	 */
	@SuppressWarnings("unchecked")
	public static <T> Future<T> postRequest(TypedHttpRequest<T> request, NetworkCallback<T> callback) {
		return postRequest(request, callback, BaseNetworkTaskFactory.instance);
	}

	/**
	 * Run an {@link com.levelup.http.TypedHttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * @param request {@link HttpRequest HTTP request} to execute to get the parsed object
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @param factory Factory used to create the {@link NetworkTask} that will download the data and send the result in the UI thread
	 * @param <T> Type of the Object generated from the parsed data
	 * @return A Future<T> representing the download task, if you need to cancel it
	 * @see #postRequest(TypedHttpRequest, NetworkCallback)
	 */
	public static <T> Future<T> postRequest(TypedHttpRequest<T> request, NetworkCallback<T> callback, NetworkTaskFactory<T> factory) {
		HttpEngine<T> httpEngine = new HttpEngine.Builder<T>().setTypedRequest(request).build();
		return postRequest(httpEngine, callback, factory);
	}

	/**
	 * Run the {@link com.levelup.http.HttpEngine HttpEngine} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * @param httpEngine {@link com.levelup.http.HttpEngine} to execute to get the parsed object
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @param factory Factory used to create the {@link NetworkTask} that will download the data and send the result in the UI thread
	 * @param <T> Type of the Object generated from the parsed data
	 * @return A Future<T> representing the download task, if you need to cancel it
	 */
	public static <T> Future<T> postRequest(HttpEngine<T> httpEngine, NetworkCallback<T> callback, NetworkTaskFactory<T> factory) {
		FutureTask<T> task = factory.createNetworkTask(httpEngine, callback);
		executor.execute(task);
		return task;
	}

	/**
	 * Run an {@link HttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * <p>The {@code tag} is used to identify similar queries so previous instances can be canceled so that only the last call gives a result.
	 * @param request {@link HttpRequest HTTP request} to execute to get the parsed object
	 * @param tag String used to match previously running similar jobs to be canceled, null to not cancel anything
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @see #postStringRequest(String, String, NetworkCallback)
	 */
	public static <T> void postRequest(TypedHttpRequest<T> request, String tag, NetworkCallback<T> callback) {
		if (TextUtils.isEmpty(tag)) {
			postRequest(request, callback);
			return;
		}

		synchronized (taggedJobs) {
			Future<?> oldTask = taggedJobs.get(tag);
			if (null != oldTask) {
				oldTask.cancel(true);
			}

			Future<T> task = postRequest(request, callback, new TaggedStringDownloadFactory<T>(tag));

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
