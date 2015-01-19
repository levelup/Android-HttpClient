package co.tophe.async;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.tophe.HttpEngine;
import co.tophe.HttpRequest;
import co.tophe.ResponseHandler;
import co.tophe.ServerException;
import co.tophe.TypedHttpRequest;

/**
 * {@link java.util.concurrent.FutureTask FutureTask} that will be used to do the HTTP processing in the background,
 * the result/error will be sent to the {@link AsyncCallback} in the UI thread
 *
 * @param <T> the type of data returned by the task.
 * @author Steve Lhomme
 * @see co.tophe.async.AsyncTask.Builder
 */
public class AsyncTask<T> extends FutureTask<T> {
	private final AsyncCallback<T> callback;
	private final boolean reportNullResult;

	private static final Handler uiHandler = new Handler(Looper.getMainLooper());

	/**
	 * Constructor to process a {@link co.tophe.TypedHttpRequest} asynchronously and call the callback when it's done.
	 *
	 * @param request  the typed HTTP request to process asynchronously.
	 * @param callback the callback to call when the processing is finished.
	 * @param <SE>     type of exception raised when the HTTP server generates an error.
	 */
	public <SE extends ServerException> AsyncTask(TypedHttpRequest<T, SE> request, AsyncCallback<T> callback) {
		this(new HttpEngine.Builder<T, SE>().setTypedRequest(request).build(), callback);
	}

	/**
	 * Handle a {@link java.util.concurrent.Callable} and call when callback when it's done. It can be an {@link co.tophe.HttpEngine}.
	 *
	 * @param callable the {@link java.util.concurrent.Callable} to run when
	 * @param callback the callback to call when the processing is finished.
	 */
	public AsyncTask(Callable<T> callable, AsyncCallback<T> callback) {
		this(callable, callback, true);
	}

	/**
	 * Handle a {@link java.util.concurrent.Callable} and call when callback when it's done. It can be an {@link co.tophe.HttpEngine}.
	 *
	 * @param callable         the {@link java.util.concurrent.Callable} to run when
	 * @param callback         the callback to call when the processing is finished.
	 * @param reportNullResult set to {@code false} if you don't want to receive {@code null} results in the callback.
	 */
	public AsyncTask(Callable<T> callable, AsyncCallback<T> callback, boolean reportNullResult) {
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
					callback.onAsyncResult(result);
				}
			} catch (CancellationException e) {
				// do nothing, the job was canceled
			} catch (InterruptedException e) {
				// do nothing, the job was canceled
			} catch (ExecutionException e) {
				callback.onAsyncFailed(e.getCause());
			} finally {
				callback.onAsyncTaskFinished(this);
			}
	}

	@Override
	public void run() {
		if (null!=callback)
			uiHandler.post(new Runnable() {
				@Override
				public void run() {
					callback.onAsyncTaskStarted(AsyncTask.this);
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
	 * Builder class to run an {@link co.tophe.HttpEngine} or any {@link java.util.concurrent.Callable} asynchronously in the TOPHE executor.
	 * <p>You may build the {@link AsyncTask} and run it yourself or call {@link #execute()} to run it right away</p>
	 *
	 * @param <T> the type of data returned by the task.
	 * @author Created by robUx4 on 03/09/2014.
	 */
	public static class Builder<T> {
		private static final HashMap<String, AsyncTask<?>> taggedJobs = new HashMap<String, AsyncTask<?>>();

		private AsyncTaskFactory<T> factory = BaseAsyncTaskFactory.INSTANCE;
		private Executor executor = AsyncTopheClient.getExecutor();
		private Callable<T> callable;
		private AsyncCallback<T> callback;
		private String taskTag;

		public Builder() {
		}

		/**
		 * Set the {@link co.tophe.TypedHttpRequest} that will be run asynchronously
		 * @param request to process asynchronously
		 * @return Current Builder
		 */
		public <SE extends ServerException> Builder<T> setTypedRequest(TypedHttpRequest<T,SE> request) {
			return setHttpEngine(new HttpEngine.Builder<T,SE>().setTypedRequest(request).build());
		}

		/**
		 * Set the HTTP request that will be run asynchronously and the {@link co.tophe.ResponseHandler} used to parse the body data
		 * @param request to process asynchronously
		 * @param responseHandler to turn the body data into type {@link T}
		 * @return Current Builder
		 */
		public <SE extends ServerException> Builder<T> setRequest(HttpRequest request, ResponseHandler<T,SE> responseHandler) {
			return setHttpEngine(new HttpEngine.Builder<T,SE>().setRequest(request).setResponseHandler(responseHandler).build());
		}

		/**
		 * Set the HTTP engine that will be run asynchronously
		 * @param httpEngine
		 * @return Current Builder
		 */
		public <SE extends ServerException> Builder<T> setHttpEngine(HttpEngine<T,SE> httpEngine) {
			return setCallable(httpEngine);
		}

		/**
		 * Set the callable to run in the TOPHE executor.
		 * @return Current Builder
		 */
		public Builder<T> setCallable(Callable<T> callable) {
			this.callable = callable;
			return this;
		}

		/**
		 * Set the callback that will receive the result of type {@link T} or error exceptions
		 * @param callback May be {@code null}
		 * @return Current Builder
		 */
		public Builder<T> setHttpAsyncCallback(@Nullable AsyncCallback<T> callback) {
			this.callback = callback;
			return this;
		}

		/**
		 * Set the {@code AsyncTask} factory in case you need to do some extra process when a AsyncTask is ran
		 * @param factory
		 * @return Current Builder
		 */
		public Builder<T> setHttpTaskFactory(@NonNull AsyncTaskFactory<T> factory) {
			this.factory = factory;
			return this;
		}

		/**
		 * Set a tag on this request so the previous {@link AsyncTask} with the same tag are cancelled
		 * @param tag
		 * @return Current Builder
		 */
		public Builder<T> setTaskTag(String tag) {
			this.taskTag = tag;
			return this;
		}

		/**
		 * Set the executor that will be used to run the {@link AsyncTask} asynchronously, in case you don't want the default one
		 * <p>Only used when calling {@link #execute()} instead of {@link #build()}</p>
		 * @param executor
		 * @return Current Builder
		 */
		public Builder<T> setExecutor(Executor executor) {
			this.executor = executor;
			return this;
		}

		/**
		 * @return The built {@link AsyncTask} to be run asynchronously.
		 * @see #execute()
		 */
		public AsyncTask<T> build() {
			if (null == factory) throw new NullPointerException("Missing factory");
			AsyncTask<T> result = factory.createAsyncTask(callable, callback);
			this.callable = null; // safety as an HttpEngine is not reusable
			return result;
		}

		/**
		 * Create the {@link AsyncTask} and run it asynchronously via the {@link java.util.concurrent.Executor}
		 * @return The {@link FutureTask} that was submitted to the {@link java.util.concurrent.Executor}
		 * @see #build()
		 */
		public AsyncTask<T> execute() {
			if (null == factory) throw new NullPointerException("Missing factory");

			final String tag = taskTag;
			if (null != taskTag) {
				callback = new DelegateAsyncCallback<T>(callback) {
					@Override
					public void onAsyncTaskFinished(AsyncTask<T> task) {
						try {
							super.onAsyncTaskFinished(task);
						} finally {
							synchronized (taggedJobs) {
								taggedJobs.remove(tag);
							}
						}
					}
				};
			}

			AsyncTask<T> task = build();

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

/*
	public <NEW_OUTPUT> AsyncTask<NEW_OUTPUT> then(NextCallable<T, NEW_OUTPUT> nextCallable) {
		new Builder<NEW_OUTPUT>()
				.setCallable(nextCallable.createCallable())
				.setExecutor(this.e)
	}
*/
}