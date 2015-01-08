package co.tophe.async;

import java.util.concurrent.Callable;

/**
* @author Created by robUx4 on 04/09/2014.
*/
public interface NextCallable<INPUT, OUTPUT> {
	Callable<OUTPUT> getNextCallable(INPUT input) throws Exception;
}
