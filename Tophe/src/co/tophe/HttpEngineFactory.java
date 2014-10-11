package co.tophe;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public interface HttpEngineFactory {

	<T, SE extends ServerException> HttpEngine<T, SE> createEngine(HttpEngine.Builder<T, SE> builder);

}
