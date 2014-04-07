package com.levelup.http.async;


public interface AsyncHttpListener<T> {
	public enum ErrorType {
		Generic, Timeout;
	}

	void onHttpSuccess(T response);
	void onHttpError(String string, ErrorType errorType);
}
