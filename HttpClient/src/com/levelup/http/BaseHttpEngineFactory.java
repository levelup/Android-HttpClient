package com.levelup.http;

import com.levelup.http.internal.HttpEngineUrlConnection;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class BaseHttpEngineFactory implements HttpEngineFactory {

	private BaseHttpEngineFactory() {
	}

	public static final BaseHttpEngineFactory INSTANCE = new BaseHttpEngineFactory();

	@Override
	public <T, SE extends ServerException> HttpEngine<T, SE> createEngine(HttpEngine.Builder<T, SE> builder) {
		return new HttpEngineUrlConnection<T, SE>(builder);
	}
}
