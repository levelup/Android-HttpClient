package com.levelup.http.paging;

import java.util.concurrent.Callable;

import com.levelup.http.async.NextCallable;

/**
* Created by robUx4 on 02/09/2014.
*/
public interface NextPageFactory<PAGE> extends NextCallable<PAGE, PAGE> {
	/**
	 * @param page The current page
	 * @return A {@link java.util.concurrent.Callable} to retrieve the next page or {@code null} if there's no more pages to read
	 */
	Callable<PAGE> getNextCallable(PAGE page);
}
