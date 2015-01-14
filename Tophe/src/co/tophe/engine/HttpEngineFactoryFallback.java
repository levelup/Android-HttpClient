package co.tophe.engine;

import co.tophe.HttpEngine;
import co.tophe.HttpEngineFactory;
import co.tophe.ServerException;

/**
 * An {@link co.tophe.HttpEngineFactory} that can fallback to another factory if the query is not supported by the main one.
 * ie if the main engine returns {@link co.tophe.engine.DummyHttpEngine}
 *
 * @author Created by robUx4 on 01/09/2014.
 */
public class HttpEngineFactoryFallback implements HttpEngineFactory {
	public final HttpEngineFactory mainFactory;
	public final HttpEngineFactory fallbackFactory;

	public HttpEngineFactoryFallback(HttpEngineFactory mainFactory, HttpEngineFactory fallbackFactory) {
		this.mainFactory = mainFactory;
		this.fallbackFactory = fallbackFactory;
	}

	@Override
	public <T, SE extends ServerException> HttpEngine<T, SE> createEngine(HttpEngine.Builder<T, SE> builder) {
		HttpEngine<T, SE> engine = mainFactory.createEngine(builder);
		if (null == engine || engine instanceof DummyHttpEngine)
			return fallbackFactory.createEngine(builder);
		return engine;
	}
}
