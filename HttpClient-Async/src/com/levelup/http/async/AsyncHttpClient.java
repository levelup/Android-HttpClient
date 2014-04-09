package com.levelup.http.async;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.text.TextUtils;

import com.levelup.http.HttpRequest;
import com.levelup.http.HttpRequestGet;
import com.levelup.http.InputStreamParser;
import com.levelup.http.InputStreamStringParser;

/**
 * Basic HttpClient to run network queries outside of the UI thread
 * 
 * @author Steve Lhomme
 */
public class AsyncHttpClient {

	private static final int THREAD_POOL_SIZE = 3*Runtime.getRuntime().availableProcessors();
	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>();

	private static final HashMap<String, Future<?>> taggedJobs = new HashMap<String, Future<?>>();

	private static ExecutorService executor = new ThreadPoolExecutor(0, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS, sPoolWorkQueue);

	private AsyncHttpClient() {
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
	public static void getString(String url, String tag, AsyncHttpCallback<String> callback) {
		HttpRequestGet req = new HttpRequestGet(url);
		getString(req, tag, callback);
	}

	/**
	 * Run an {@link HttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * @param request {@link HttpRequest HTTP request} to execute to get the parsed object
	 * @param parser Parser to transform the HTTP response into your object, in the network thread. Must not be {@code null}
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @return A Future<T> representing the download task, if you need to cancel it
	 */
	public static <T> Future<T> doRequest(final HttpRequest request, final InputStreamParser<T> parser, AsyncHttpCallback<T> callback) {
		return doRequest(request, parser, callback, DownloadTaskFactory.BaseDownloadTaskFactory.instance);
	}

	/**
	 * Run an {@link HttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * @param request {@link HttpRequest HTTP request} to execute to get the parsed object
	 * @param parser Parser to transform the HTTP response into your object, in the network thread. Must not be {@code null}
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @param factory Factory used to create the {@link DownloadTask} that will download the data and send the result in the UI thread
	 * @return A Future<T> representing the download task, if you need to cancel it
	 * @see #doRequest(HttpRequest, InputStreamParser, AsyncHttpCallback)
	 */
	public static <T> Future<T> doRequest(final HttpRequest request, final InputStreamParser<T> parser, AsyncHttpCallback<T> callback, DownloadTaskFactory<T> factory) {
		return doRequest(executor, request, parser, callback, factory);
	}

	/**
	 * Run an {@link HttpRequest HTTP request} in the thread executor and post the resulting parsed object to {@code callback} in the UI thread.
	 * @param executor The executor to use for the downloading thread
	 * @param request {@link HttpRequest HTTP request} to execute to get the parsed object
	 * @param parser Parser to transform the HTTP response into your object, in the network thread. Must not be {@code null}
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @param factory Factory used to create the {@link DownloadTask} that will download the data and send the result in the UI thread
	 * @return A Future<T> representing the download task, if you need to cancel it
	 * @see #doRequest(HttpRequest, String, InputStreamParser, AsyncHttpCallback)
	 */
	public static <T> Future<T> doRequest(Executor executor, final HttpRequest request, final InputStreamParser<T> parser, AsyncHttpCallback<T> callback, DownloadTaskFactory<T> factory) {
		if (null==parser) throw new InvalidParameterException();

		FutureTask<T> task = factory.createDownloadTask(request, parser, callback);
		executor.execute(task);
		return task;
	}
	
	/**
	 * Do an {@link HttpRequest} query to load a String though this asynchronous client
	 * @param request {@link HttpRequest HTTP request} to execute
	 * @param tag String used to match previously running similar jobs to be canceled, null to not cancel anything
	 * @param callback Callback receiving the String or errors (not job canceled) in the UI thread. May be {@code null}
	 * @see #getString(String, String, AsyncHttpCallback)
	 */
	public static void getString(final HttpRequest request, final String tag, final AsyncHttpCallback<String> callback) {
		doRequest(request, tag, InputStreamStringParser.instance, callback);
	}

	/**
	 * Run an {@link HttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * <p>The {@code tag} is used to identify similar queries so previous instances can be canceled so that only the last call gives a result.
	 * @param request {@link HttpRequest HTTP request} to execute to get the parsed object
	 * @param tag String used to match previously running similar jobs to be canceled, null to not cancel anything
	 * @param parser Parser to transform the HTTP response into your object, in the network thread. Must not be {@code null}
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @see #getString(HttpRequest, String, AsyncHttpCallback)
	 */
	public static <T> void doRequest(final HttpRequest request, String tag, final InputStreamParser<T> parser, AsyncHttpCallback<T> callback) {
		if (null==parser) throw new InvalidParameterException();

		if (TextUtils.isEmpty(tag)) {
			doRequest(request, parser, callback);
		}

		synchronized (taggedJobs) {
			if (null!=tag) {
				Future<?> oldTask = taggedJobs.get(tag);
				if (null != oldTask) {
					oldTask.cancel(true);
				}
			}

			Future<T> task = doRequest(request, parser, callback, new TaggedStringDownloadFactory<T>(tag));

			if (null!=tag) {
				taggedJobs.put(tag, task);
			}
		}
	}

	/**
	 * A {@link DownloadTaskFactory} that will remove the tag from the running tasks when done
	 * @param <T>
	 */
	private static class TaggedStringDownloadFactory<T> implements DownloadTaskFactory<T> {
		private final String tag;

		TaggedStringDownloadFactory(String tag) {
			this.tag = tag;
		}

		@Override
		public DownloadTask<T> createDownloadTask(HttpRequest request, InputStreamParser<T> parser, AsyncHttpCallback<T> callback) {
			return new DownloadTask<T>(request, parser, callback) {
				@Override
				protected void onDownloadDone() {
					try {
						super.onDownloadDone();
					} finally {
						if (null!=tag) {
							synchronized (taggedJobs) {
								taggedJobs.remove(tag);
							}
						}
					}
				};
			};
		}
	};
}
