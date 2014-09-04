package com.levelup.http.async;

import java.util.concurrent.Callable;

/**
 * Created by robUx4 on 04/09/2014.
 */
public final class AsyncHelper {
	private AsyncHelper() {
	}

	public static <OUTPUT, INPUT> Callable<OUTPUT> chain(final Callable<INPUT> inputCallable, final NextCallable<INPUT, OUTPUT> nextPageFactory) {
		return new Callable<OUTPUT>() {
			@Override
			public OUTPUT call() throws Exception {
				INPUT input = inputCallable.call();
				Callable<OUTPUT> nextPageCall = nextPageFactory.getNextCallable(input);
				if (null == nextPageCall)
					return null;
				return nextPageCall.call();
			}
		};
	}
}
