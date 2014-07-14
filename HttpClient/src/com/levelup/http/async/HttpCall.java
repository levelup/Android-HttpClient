package com.levelup.http.async;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpEngine;
import com.levelup.http.HttpException;
import com.levelup.http.HttpResponse;
import com.levelup.http.InputStreamParser;
import com.levelup.http.TypedHttpRequest;

public class HttpCall<T> {
	
	private static final ExecutorService DEFAULT_EXECUTOR = AsyncHttpClient.getExecutorService();

	public static class Builder<T> {
		final TypedHttpRequest<T> request;
		String tag;
		ExecutorService executor = DEFAULT_EXECUTOR;
		@SuppressWarnings("unchecked")
		NetworkTaskFactory<T> taskFactory = BaseNetworkTaskFactory.instance;
		
		public Builder(String url) {
			this(url, null);
		}
		
		public Builder(String url, InputStreamParser<T> parser) {
			this(new BaseHttpRequest.Builder<T>(HttpClient.defaultContext)
					.setUrl(url)
					.setStreamParser(parser)
					.build());
		}
		
		public Builder(TypedHttpRequest<T> request) {
			this.request = request;
		}
		
		public HttpCall<T> build() {
			return new HttpCall<T>(this);
		}
		
		public Builder<T> executor(ExecutorService executor) {
			this.executor = executor;
			return this;
		}
		
		public Builder<T> taskFactory(NetworkTaskFactory<T> taskFactory) {
			this.taskFactory = taskFactory;
			return this;
		}
		
		public Builder<T> tag(String tag) {
			this.tag = tag;
			return this;
		}
	}

	private final TypedHttpRequest<T> request;
	private final String tag;
	private final ExecutorService executor;
	private final NetworkTaskFactory<T> taskFactory;
	private NetworkTask<T> runningTask;

	protected HttpCall(Builder<T> builder) {
		this.request = builder.request;
		this.tag = builder.tag;
		this.executor = builder.executor;
		this.taskFactory = builder.taskFactory;
	}
	
	public T execute() throws HttpException {
		try {
			return new HttpCallable<T>(request).call();
		} catch (HttpException forward) {
			throw forward;
		} catch (Exception ignored) {
		}
		return null;
	}
	
	public void enqueue(NetworkCallback<T> callback) {
		synchronized (this) {
			if (runningTask!=null) throw new IllegalStateException("Already Running");
			runningTask = taskFactory.createNetworkTask(new HttpCallable<T>(request), callback);
		}
		executor.execute(runningTask);
	}
	
	public void cancel() {
		synchronized (this) {
			if (runningTask!=null)
				runningTask.cancel(true);

			HttpResponse response = request.getResponse();
			if (null!=response)
				response.disconnect();
			
			runningTask = null;
		}
	}
	
	public T get() throws CancellationException, InterruptedException, ExecutionException {
		final NetworkTask<T> task;
		synchronized (this) {
			if (runningTask==null) throw new IllegalStateException("Not Running");
			task = runningTask;
		}
		return task.get();
	}

}
