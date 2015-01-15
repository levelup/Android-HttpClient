package co.tophe.async;

import java.util.concurrent.Callable;

/**
 * Helper class to chain {@link java.util.concurrent.Callable} to do transformations on them in the same call.
 *
 * @param <INPUT>  the type returned by the {@link java.util.concurrent.Callable}
 * @param <OUTPUT> the type returned by the {@link co.tophe.async.NextCallable}
 * @author Created by robUx4 on 04/09/2014.
 * @see co.tophe.async.NextCallable
 */
public class CallableChain<INPUT, OUTPUT> implements Callable<OUTPUT> {
	public final Callable<INPUT> inputCallable;
	public final NextCallable<INPUT, OUTPUT> postProcess;
	private Callable<OUTPUT> nextPageCall;

	/**
	 * @param inputCallable the main {@link java.util.concurrent.Callable} to execute.
	 * @param postProcess the transformation of the data returned by the first {@link java.util.concurrent.Callable}.
	 */
	public CallableChain(Callable<INPUT> inputCallable, NextCallable<INPUT, OUTPUT> postProcess) {
		this.inputCallable = inputCallable;
		this.postProcess = postProcess;
	}

	@Override
	public OUTPUT call() throws Exception {
		INPUT input = inputCallable.call();
		nextPageCall = postProcess.createCallable(input);
		if (null == nextPageCall)
			return null;
		return nextPageCall.call();
	}

	public Callable<OUTPUT> getSecondCallable() {
		return nextPageCall;
	}
}
