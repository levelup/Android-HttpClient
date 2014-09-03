package com.levelup.http.async;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.text.TextUtils;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpRequest;
import com.levelup.http.TypedHttpRequest;
import com.levelup.http.parser.BodyToString;

/**
 * Basic HttpClient to run network queries outside of the UI thread
 * <p>Helper class for {@link com.levelup.http.async.AsyncHttpBuilder}</p>
 * 
 * @author Steve Lhomme
 */
public class AsyncHttpClient {

	private static final int THREAD_POOL_SIZE = 3*Runtime.getRuntime().availableProcessors();
	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>();

	private static final HashMap<String, HttpTask<?>> taggedJobs = new HashMap<String, HttpTask<?>>();

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
	public static void postStringRequest(String url, String tag, HttpAsyncCallback<String> callback) {
		BaseHttpRequest.Builder<String> reqBuilder = new BaseHttpRequest.Builder<String>();
		reqBuilder.setUrl(url)
				.setResponseHandler(BodyToString.RESPONSE_HANDLER);
		postTagRequest(reqBuilder.build(), tag, callback);
	}

	/**
	 * Run an {@link TypedHttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * @param request {@link TypedHttpRequest HTTP request} to execute to get the parsed object
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @param <T> Type of the Object generated from the parsed data
	 * @return A Future<T> representing the download task, if you need to cancel it
	 */
	@SuppressWarnings("unchecked")
	public static <T> HttpTask<T> postRequest(TypedHttpRequest<T> request, HttpAsyncCallback<T> callback) {
		return new AsyncHttpBuilder<T>()
				.setTypedRequest(request)
				.setHttpAsyncCallback(callback)
				.runTask();
	}

	/**
	 * Run an {@link com.levelup.http.TypedHttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * @param request {@link HttpRequest HTTP request} to execute to get the parsed object
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @param factory Factory used to create the {@link HttpTask} that will download the data and send the result in the UI thread
	 * @param <T> Type of the Object generated from the parsed data
	 * @return A Future<T> representing the download task, if you need to cancel it
	 * @see #postRequest(TypedHttpRequest, HttpAsyncCallback)
	 */
	public static <T> HttpTask<T> postRequest(TypedHttpRequest<T> request, HttpAsyncCallback<T> callback, HttpTaskFactory<T> factory) {
		return new AsyncHttpBuilder<T>()
				.setTypedRequest(request)
				.setHttpAsyncCallback(callback)
				.setHttpTaskFactory(factory)
				.runTask();
	}

	/**
	 * Run an {@link com.levelup.http.TypedHttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * <p>The {@code tag} is used to identify similar queries so previous instances can be canceled so that only the last call gives a result.
	 * @param request {@link HttpRequest HTTP request} to execute to get the parsed object
	 * @param tag String used to match previously running similar jobs to be canceled, null to not cancel anything
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 */
	public static <T> void postTagRequest(TypedHttpRequest<T> request, String tag, HttpAsyncCallback<T> callback) {
		final HttpTask<T> task;

		final AsyncHttpBuilder<T> taskBuilder = new AsyncHttpBuilder<T>()
				.setTypedRequest(request)
				.setHttpAsyncCallback(callback);

		if (TextUtils.isEmpty(tag)) {
			task = taskBuilder.buildTask();
		} else {
			taskBuilder.setHttpTaskFactory(new TaggedStringDownloadFactory<T>(tag));
			task = taskBuilder.buildTask();

			synchronized (taggedJobs) {
				HttpTask<?> oldTask = taggedJobs.get(tag);
				if (null != oldTask) {
					oldTask.cancel(true);
				}

				taggedJobs.put(tag, task);
			}
		}
		executor.execute(task);
	}

	/**
	 * A {@link HttpTaskFactory} that will remove the tag from the running tasks when done
	 * @param <T>
	 */
	private static class TaggedStringDownloadFactory<T> implements HttpTaskFactory<T> {
		private final String tag;

		TaggedStringDownloadFactory(String tag) {
			this.tag = tag;
		}

		@Override
		public HttpTask<T> createHttpTask(Callable<T> callable, HttpAsyncCallback<T> callback) {
			return new HttpTask<T>(callable, callback) {
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
