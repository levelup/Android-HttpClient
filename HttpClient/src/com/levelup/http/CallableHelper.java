package com.levelup.http;

import java.util.LinkedList;
import java.util.concurrent.Callable;

/**
 * Created by robUx4 on 30/08/2014.
 */
public class CallableHelper {
	public interface NextPageFactory<PAGE> {
		/**
		 * @param page The current page
		 * @return A {@link java.util.concurrent.Callable} to retrieve the next page or {@code null}
		 */
		Callable<PAGE> createNextPageCallable(PAGE page);
	}

	public static <PAGE> Callable<LinkedList<PAGE>> readPages(final Callable<PAGE> pageRequest, final NextPageFactory<PAGE> nextPageFactory) {
		return processPage(pageRequest,
				new PageDataProcessor<LinkedList<PAGE>, PAGE>() {
					@Override
					public Callable<PAGE> addPageAndContinue(LinkedList<PAGE> pages, PAGE page) {
						pages.add(page);
						return nextPageFactory.createNextPageCallable(page);
					}
				},
				new LinkedList<PAGE>());
	}

	public interface PageDataProcessor<PAGED_HOLDER, PAGE> {
		/**
		 * Add the {@code page} to the {@code pageHolder} and return the {@code Callable} to retrieve the next page
		 * @param pagedHolder Object that will retain the data from the next page
		 * @param page The new page received
		 * @return A {@link java.util.concurrent.Callable} to retrieve the next page
		 */
		Callable<PAGE> addPageAndContinue(PAGED_HOLDER pagedHolder, PAGE page);
	}

	/**
	 * Read a {@link PAGE}, store it and get the next page if there is one
	 * @param pageRequest Request to get the {@link PAGE} data
	 * @param pageDataProcessor Callback to handle the new page and generate the request to get the next page
	 * @param pagedHolder Object that will keep all the pages
	 * @param <PAGED_HOLDER> Type of the Object that will hold all the pages we receive
	 * @param <PAGE> Type of the Object representing a page
	 * @return
	 */
	public static <PAGED_HOLDER, PAGE> Callable<PAGED_HOLDER> processPage(final Callable<PAGE> pageRequest, final PageDataProcessor<PAGED_HOLDER, PAGE> pageDataProcessor, final PAGED_HOLDER pagedHolder) {
		return new Callable<PAGED_HOLDER>() {
			@Override
			public PAGED_HOLDER call() throws Exception {
				PAGE newPage = pageRequest.call();
				Callable<PAGE> nextPageCall = pageDataProcessor.addPageAndContinue(pagedHolder, newPage);
				if (null == nextPageCall)
					return pagedHolder;
				return processPage(nextPageCall, pageDataProcessor, pagedHolder).call();
			}
		};
	}

	/**
	 * Chain a {@link java.util.concurrent.Callable} and a {@link com.levelup.http.CallableHelper.CallableForResult}
	 * <p>The {@code Callable} is processed and the result passed to the {@link com.levelup.http.CallableHelper.CallableForResult} to
	 * produce another {@link java.util.concurrent.Callable} for further processing, {@code null} otherwise</p>
	 * @param mainCall The main {@code Callable} to process the data
	 * @param callableForResult A callback to generate an extra {@code Callable} to process the data further
	 * @param <A> Output type of the main {@code Callable}
	 * @param <B> Output type of the generated {@code Callable}
	 * @return A {@link java.util.concurrent.Callable} to process data further, or {@code null} if no more processing is needed
	 */
	public static <A, B> Callable<B> chainCallable(final Callable<A> mainCall, final CallableForResult<A, B> callableForResult) {
		return new Callable<B>() {
			public B call() throws Exception {
				A result = mainCall.call();
				Callable<B> postCallable = callableForResult.getNextCallable(result);
				if (null==postCallable)
					return null;
				return postCallable.call();
			}
		};
	}

	public static interface CallableForResult<A, B> {
		/**
		 * Transform {@code A} to a {@link java.util.concurrent.Callable} of output type {@code B}.
		 *
		 * @param a The data to process.
		 * @return A new {@link java.util.concurrent.Callable} to process the new data or {@code null} if no further processing is needed
		 */
		Callable<B> getNextCallable(A a) throws HttpException;
	}
}
