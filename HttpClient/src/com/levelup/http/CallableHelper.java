package com.levelup.http;

import java.util.concurrent.Callable;

/**
 * Created by robUx4 on 30/08/2014.
 */
public class CallableHelper {
	/**
	 * Chain a {@link java.util.concurrent.Callable} and a {@link com.levelup.http.CallableHelper.CallableForResult}
	 * <p>The {@code Callable} is processed and the result passed to the {@link com.levelup.http.CallableHelper.CallableForResult} to
	 * produce another {@link java.util.concurrent.Callable} for further processing, {@code null} otherwise</p>
	 * @param mainCall The main {@code Callable} to process the data
	 * @param callableForResult A callback to generate an extra {@code Callable} to process the data further
	 * @param <A> Output type of the main {@code Callable}
	 * @param <B> Output type of the generated {@code Callable}
	 * @return A {@link java.util.concurrent.Callable} to process data further, or {@code null} if no more processing is needed
	 */
	public static <A, B> Callable<B> chainCallable(final Callable<A> mainCall, final CallableForResult<A, B> callableForResult) {
		return new Callable<B>() {
			public B call() throws Exception {
				A result = mainCall.call();
				Callable<B> postCallable = callableForResult.getNextCallable(result);
				if (null==postCallable)
					return null;
				return postCallable.call();
			}
		};
	}

	public static interface CallableForResult<A, B> {
		/**
		 * Transform {@code A} to a {@link java.util.concurrent.Callable} of output type {@code B}.
		 *
		 * @param a The data to process.
		 * @return A new {@link java.util.concurrent.Callable} to process the new data or {@code null} if no further processing is needed
		 */
		Callable<B> getNextCallable(A a) throws HttpException;
	}

	// TODO might return a Callable or the end result, see what is best with the examples

}
