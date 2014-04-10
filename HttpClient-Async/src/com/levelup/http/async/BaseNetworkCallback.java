package com.levelup.http.async;

public class BaseNetworkCallback<T> implements NetworkCallback<T> {

	@Override
	public void onNetworkSuccess(T result) {
	}

	@Override
	public void onNetworkFailed(Throwable t) {
	}

	@Override
	public void onNetworkStarted(NetworkTask<T> task) {
	}

	@Override
	public void onNetworkFinished(NetworkTask<T> task) {
	}
}
