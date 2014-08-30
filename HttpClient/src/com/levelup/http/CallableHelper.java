package com.levelup.http;

import java.util.concurrent.Callable;

/**
 * Created by robUx4 on 30/08/2014.
 */
public class CallableHelper {
	public static <A, B> Callable<B> chain(final Callable<A> a, final CallableForResult<A, B> callableForResult) {
		return new Callable<B>() {
			public B call() throws Exception {
				Callable<B> postCallable = callableForResult.getResultCallable(a.call());
				if (null==postCallable)
					return null;
				return postCallable.call();
			}
		};
	}

	// TODO might return a Callable or the end result, see what is best with the examples

	public static interface CallableForResult<A, B> {
		/**
		 * Transform {@code A} to {@code B}.
		 *
		 * @param a The {@code A} to transform.
		 * @return A new {@link java.util.concurrent.Callable} to process the new data or {@code null} if no further processing is needed
		 */
		public abstract Callable<B> getResultCallable(A a) throws HttpException;
	}
}
