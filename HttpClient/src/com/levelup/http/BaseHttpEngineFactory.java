package com.levelup.http;

import com.levelup.http.internal.HttpEngineUrlConnection;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class BaseHttpEngineFactory implements HttpEngineFactory {

	private BaseHttpEngineFactory() {
	}

	public static BaseHttpEngineFactory instance = new BaseHttpEngineFactory();

	@Override
	public <T> HttpEngine<T,?> createHttpEngine(BaseHttpRequest.AbstractBuilder<T, ?> builder) {
		return new HttpEngineUrlConnection<T>(builder);
	}
}
