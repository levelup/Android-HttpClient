package co.tophe.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.support.annotation.Nullable;

import co.tophe.TypedHttpRequest;

/**
 * Basic TopheClient to run network queries outside of the UI thread.
 * <p>Helper class for {@link AsyncTask.Builder}</p>
 * 
 * @author Steve Lhomme
 */
public class AsyncTopheClient {

	private static final int THREAD_POOL_SIZE = 3*Runtime.getRuntime().availableProcessors();
	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>();

	private static Executor executor;
	static {
		executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60, TimeUnit.SECONDS, sPoolWorkQueue);
		((ThreadPoolExecutor) executor).allowCoreThreadTimeOut(true);
	}

	private AsyncTopheClient() {
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
	 * Run an {@link TypedHttpRequest HTTP request} in the background and post the resulting parsed object to {@code callback} in the UI thread.
	 * @param request {@link TypedHttpRequest HTTP request} to execute to get the parsed object
	 * @param callback Callback receiving the parsed object or errors (not job canceled) in the UI thread. May be {@code null}
	 * @param <T> Type of the Object generated from the parsed data
	 * @return A Future<T> representing the download task, if you need to cancel it
	 */
	@SuppressWarnings("unchecked")
	public static <T> AsyncTask<T> postRequest(TypedHttpRequest<T,?> request, @Nullable AsyncCallback<T> callback) {
		return new AsyncTask.Builder<T>()
				.setTypedRequest(request)
				.setHttpAsyncCallback(callback)
				.execute();
	}

}
