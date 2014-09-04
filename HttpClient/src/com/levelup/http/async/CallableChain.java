package com.levelup.http.async;

import java.util.concurrent.Callable;

/**
 * Created by robUx4 on 04/09/2014.
 */
public class CallableChain<INPUT, OUTPUT> implements Callable<OUTPUT> {
	public final Callable<INPUT> inputCallable;
	public final NextCallable<INPUT, OUTPUT> postProcess;
	private Callable<OUTPUT> nextPageCall;

	public CallableChain(Callable<INPUT> inputCallable, NextCallable<INPUT, OUTPUT> postProcess) {
		this.inputCallable = inputCallable;
		this.postProcess = postProcess;
	}

	@Override
	public OUTPUT call() throws Exception {
		INPUT input = inputCallable.call();
		nextPageCall = postProcess.getNextCallable(input);
		if (null == nextPageCall)
			return null;
		return nextPageCall.call();
	}

	public Callable<OUTPUT> getSecondCallable() {
		return nextPageCall;
	}
}
