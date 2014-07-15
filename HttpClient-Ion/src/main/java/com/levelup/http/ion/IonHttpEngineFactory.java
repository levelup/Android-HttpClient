package com.levelup.http.ion;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpEngine;
import com.levelup.http.HttpEngineFactory;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonHttpEngineFactory implements HttpEngineFactory {
	private final HttpEngineFactory nonStreamingFactory;

	public IonHttpEngineFactory(HttpEngineFactory nonStreamingFactory) {
		this.nonStreamingFactory = nonStreamingFactory;
	}

	@Override
	public <T> HttpEngine<T> createHttpEngine(BaseHttpRequest.AbstractBuilder<T, ?> builder) {
		if (builder.isStreaming())
			return nonStreamingFactory.createHttpEngine(builder);
		return new HttpEngineIon<T>(builder);
	}
}
