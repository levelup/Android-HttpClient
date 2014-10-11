package co.tophe.paging;

import java.util.concurrent.Callable;

import android.support.annotation.Nullable;

import co.tophe.async.NextCallable;

/**
* Created by robUx4 on 02/09/2014.
*/
public interface NextPageFactory<PAGE> extends NextCallable<PAGE, PAGE> {
	/**
	 * @param page The current page
	 * @return A {@link java.util.concurrent.Callable} to retrieve the next page or {@code null} if there's no more pages to read
	 */
	@Nullable
	Callable<PAGE> getNextCallable(PAGE page);
}
