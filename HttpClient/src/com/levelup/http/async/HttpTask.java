package com.levelup.http.async;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import android.os.Handler;
import android.os.Looper;

import com.levelup.http.HttpEngine;
import com.levelup.http.HttpRequest;
import com.levelup.http.ResponseHandler;
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

	/**
	 * Builder class to run an {@link com.levelup.http.HttpEngine} asynchronously
	 * <p>You may build the {@link HttpTask} and run it yourself or call {@link #execute()} to run it right away</p>
	 * @author Created by robUx4 on 03/09/2014.
	 */
	public static class Builder<T> {
		private static final HashMap<String, HttpTask<?>> taggedJobs = new HashMap<String, HttpTask<?>>();

		private HttpTaskFactory<T> factory = BaseHttpTaskFactory.instance;
		private Executor executor = AsyncHttpClient.getExecutor();
		private HttpEngine<T> httpEngine;
		private HttpAsyncCallback<T> callback;
		private String taskTag;

		public Builder() {
		}

		/**
		 * Set the {@link com.levelup.http.TypedHttpRequest} that will be run asynchronously
		 * @param request to process asynchronously
		 * @return Current Builder
		 */
		public Builder<T> setTypedRequest(TypedHttpRequest<T> request) {
			return setHttpEngine(new HttpEngine.Builder<T>().setTypedRequest(request).build());
		}

		/**
		 * Set the HTTP request that will be run asynchronously and the {@link com.levelup.http.ResponseHandler} used to parse the body data
		 * @param request to process asynchronously
		 * @param responseHandler to turn the body data into type {@link T}
		 * @return Current Builder
		 */
		public Builder<T> setRequest(HttpRequest request, ResponseHandler<T> responseHandler) {
			return setHttpEngine(new HttpEngine.Builder<T>().setRequest(request).setResponseHandler(responseHandler).build());
		}

		/**
		 * Set the HTTP engine that will be run asynchronously
		 * @param httpEngine
		 * @return Current Builder
		 */
		public Builder<T> setHttpEngine(HttpEngine<T> httpEngine) {
			this.httpEngine = httpEngine;
			return this;
		}

		/**
		 * Set the callback that will receive the result of type {@link T} or error exceptions
		 * @param callback May be {@code null}
		 * @return Current Builder
		 */
		public Builder<T> setHttpAsyncCallback(HttpAsyncCallback<T> callback) {
			this.callback = callback;
			return this;
		}

		/**
		 * Set the {@code HttpTask} factory in case you need to do some extra process when a HttpTask is ran
		 * @param factory
		 * @return Current Builder
		 */
		public Builder<T> setHttpTaskFactory(HttpTaskFactory<T> factory) {
			this.factory = factory;
			return this;
		}

		/**
		 * Set a tag on this request so the previous {@link HttpTask} with the same tag are cancelled
		 * @param tag
		 * @return Current Builder
		 */
		public Builder<T> setTaskTag(String tag) {
			this.taskTag = tag;
			return this;
		}

		/**
		 * Set the executor that will be used to run the {@link HttpTask} asynchronously, in case you don't want the default one
		 * @param executor
		 * @return Current Builder
		 */
		public Builder<T> setExecutor(Executor executor) {
			this.executor = executor;
			return this;
		}

		/**
		 * @return The {@link HttpTask} to be run asynchronously
		 */
		public HttpTask<T> buildTask() {
			if (null == factory) throw new NullPointerException("Missing factory");
			HttpTask<T> result = factory.createHttpTask(httpEngine, callback);
			this.httpEngine = null; // safety as an HttpEngine is not reusable
			return result;
		}

		/**
		 * Create the {@link HttpTask} and run it asynchronously via the {@link java.util.concurrent.Executor}
		 * @return The {@link FutureTask} that was submitted to the {@link java.util.concurrent.Executor}
		 */
		public HttpTask<T> execute() {
			if (null == factory) throw new NullPointerException("Missing factory");

			final String tag = taskTag;
			if (null != taskTag) {
				callback = new DelegateHttpAsyncCallback<T>(callback) {
					@Override
					public void onHttpTaskFinished(HttpTask<T> task) {
						try {
							super.onHttpTaskFinished(task);
						} finally {
							synchronized (taggedJobs) {
								taggedJobs.remove(tag);
							}
						}
					}
				};
			}

			HttpTask<T> task = buildTask();

			if (null != taskTag) {
				synchronized (taggedJobs) {
					Future<?> oldTask = taggedJobs.get(tag);
					if (null != oldTask) {
						oldTask.cancel(true);
					}

					taggedJobs.put(tag, task);
				}
			}

			executor.execute(task);
			return task;
		}
	}
}