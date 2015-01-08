package co.tophe.paging;

/**
* @author Created by robUx4 on 02/09/2014.
*/
public interface PageCallback<PAGE_HOLDER, PAGE> {
	/**
	 * Add the {@code page} to the {@code pageHolder} and return the {@code Callable} to retrieve the next page
	 * @param pagesHolder Object that will retain the data from the next page
	 * @param page The new page received
	 */
	void onNewPage(PAGE_HOLDER pagesHolder, PAGE page);
}
