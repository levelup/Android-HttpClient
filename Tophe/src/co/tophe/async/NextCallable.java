package co.tophe.async;

import java.util.concurrent.Callable;

/**
 * Interface to generate a Callable for the {@link INPUT} data that will return data of {@link OUTPUT} type.
 *
 * @author Created by robUx4 on 04/09/2014.
 */
public interface NextCallable<INPUT, OUTPUT> {
	/**
	 * Create a {@link java.util.concurrent.Callable} to process the {@link INPUT} data.
	 * @throws Exception
	 */
	Callable<OUTPUT> createCallable(INPUT input) throws Exception;
}
