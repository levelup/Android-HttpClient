package com.levelup.http.async;

import java.util.concurrent.Executor;

import com.levelup.http.HttpEngine;
import com.levelup.http.HttpRequest;
import com.levelup.http.RawHttpRequest;
import com.levelup.http.ResponseHandler;
import com.levelup.http.TypedHttpRequest;

/**
 * Builder class to run an {@link com.levelup.http.HttpEngine} asynchronously
 * <p>You may build the {@link com.levelup.http.async.HttpTask} and run it yourself or call {@link #runTask()} to run it right away</p>
 * @author Created by robUx4 on 03/09/2014.
 */
public class AsyncHttpBuilder<T> {
	private HttpTaskFactory<T> factory = BaseHttpTaskFactory.instance;
	private Executor executor = AsyncHttpClient.getExecutor();
	private HttpEngine<T> httpEngine;
	private HttpAsyncCallback<T> callback;

	public AsyncHttpBuilder() {
	}

	/**
	 * Set the {@link com.levelup.http.TypedHttpRequest} that will be run asynchronously
	 * @param request to process asynchronously
	 * @return Current Builder
	 */
	public AsyncHttpBuilder<T> setTypedRequest(TypedHttpRequest<T> request) {
		return setHttpEngine(new HttpEngine.Builder<T>().setTypedRequest(request).build());
	}

	/**
	 * Set the HTTP request that will be run asynchronously and the {@link com.levelup.http.ResponseHandler} used to parse the body data
	 * @param request to process asynchronously
	 * @param responseHandler to turn the body data into type {@link T}
	 * @return Current Builder
	 */
	public AsyncHttpBuilder<T> setTypedRequest(HttpRequest request, ResponseHandler<T> responseHandler) {
		return setHttpEngine(new HttpEngine.Builder<T>().setRequest(request).setResponseHandler(responseHandler).build());
	}

	/**
	 * Set the HTTP engine that will be run asynchronously
	 * @param httpEngine
	 * @return Current Builder
	 */
	public AsyncHttpBuilder<T> setHttpEngine(HttpEngine<T> httpEngine) {
		this.httpEngine = httpEngine;
		return this;
	}

	/**
	 * Set the callback that will receive the result of type {@link T} or error exceptions
	 * @param callback May be {@code null}
	 * @return Current Builder
	 */
	public AsyncHttpBuilder<T> setHttpAsyncCallback(HttpAsyncCallback<T> callback) {
		this.callback = callback;
		return this;
	}

	/**
	 * Set the {@code HttpTask} factory in case you need to do some extra process when a HttpTask is ran
	 * @param factory
	 * @return Current Builder
	 * @see com.levelup.http.async.AsyncHttpClient.TaggedStringDownloadFactory
	 */
	public AsyncHttpBuilder<T> setHttpTaskFactory(HttpTaskFactory<T> factory) {
		this.factory = factory;
		return this;
	}

	/**
	 * Set the executor that will be used to run the {@link com.levelup.http.async.HttpTask} asynchronously, in case you don't want the default one
	 * @param executor
	 * @return Current Builder
	 */
	public AsyncHttpBuilder<T> setExecutor(Executor executor) {
		this.executor = executor;
		return this;
	}

	/**
	 * @return The {@link com.levelup.http.async.HttpTask} to be run asynchronously
	 */
	public HttpTask<T> buildTask() {
		if (null == factory) throw new NullPointerException("Missing factory");
		HttpTask<T> result = factory.createHttpTask(httpEngine, callback);
		this.httpEngine = null; // safety as an HttpEngine is not reusable
		return result;
	}

	/**
	 * Create the {@link com.levelup.http.async.HttpTask} and run it asynchronously via the {@link java.util.concurrent.Executor}
	 * @return The {@link com.levelup.http.async.HttpTask} that was submitted to the {@link java.util.concurrent.Executor}
	 */
	public HttpTask<T> runTask() {
		HttpTask<T> task = buildTask();
		executor.execute(task);
		return task;
	}
}
