package com.levelup.http;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public interface HttpEngineFactory {

	<T> HttpEngine<T> createEngine(HttpEngine.Builder<T> builder);

}
