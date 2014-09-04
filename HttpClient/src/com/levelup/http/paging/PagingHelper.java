package com.levelup.http.paging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import com.levelup.http.async.AsyncHttpClient;
import com.levelup.http.async.HttpAsyncCallback;
import com.levelup.http.async.HttpTask;

/**
 * Created by robUx4 on 02/09/2014.
 */
public class PagingHelper {

	/**
	 * Read all the pages starting with the current one
	 *
	 * @param currentPageRequest Request to get the current {@link PAGE} data (usually a {@link com.levelup.http.HttpEngine HttpEngine})
	 * @param nextPageFactory    Factory to get the {@link java.util.concurrent.Callable} to retrieve the next page
	 * @param <PAGE>             Type of the Object representing a page
	 * @return A {@link java.util.concurrent.Callable} to get all the pages in a {@link java.util.List} in the order they were read
	 */
	public static <PAGE> Callable<List<PAGE>> readPages(Callable<PAGE> currentPageRequest, NextPageFactory<PAGE> nextPageFactory) {
		return processPage(currentPageRequest,
				new ArrayList<PAGE>(), new PageCallback<List<PAGE>, PAGE>() {
					@Override
					public void onNewPage(List<PAGE> pagesHolder, PAGE page) {
						pagesHolder.add(page);
					}
				}
				, nextPageFactory);
	}

	/**
	 * Read a {@link PAGE}, store it and read the next page recursively until there's none
	 *
	 * @param currentPageRequest Request to get the current {@link PAGE} data (usually a {@link com.levelup.http.HttpEngine HttpEngine})
	 * @param pagedHolder        Object that will keep all the pages
	 * @param pageCallback       Callback to handle the loaded {@link PAGE} with the {@link PAGE_HOLDER}, may be {@code null}
	 * @return A {@link java.util.concurrent.Callable} to get all the pages
	 */
	public static <PAGE_HOLDER, PAGE> Callable<PAGE_HOLDER> processPage(final Callable<PAGE> currentPageRequest, final PAGE_HOLDER pagedHolder,
	                                                                    final PageCallback<PAGE_HOLDER, PAGE> pageCallback,
	                                                                    final NextPageFactory<PAGE> nextPageFactory) {
		return new Callable<PAGE_HOLDER>() {
			@Override
			public PAGE_HOLDER call() throws Exception {
				PAGE newPage = currentPageRequest.call();
				if (null!=pageCallback)
					pageCallback.onNewPage(pagedHolder, newPage);
				Callable<PAGE> nextPageCall = nextPageFactory.createNextPageCallable(newPage);
				if (null == nextPageCall)
					return pagedHolder;
				return processPage(nextPageCall, pagedHolder, pageCallback, nextPageFactory).call();
			}
		};
	}

	/**
	 * Read all the {@link PAGE} and give the {@code resultCallback} these pages in a {@link List}
	 *
	 * @param currentPageRequest Request to get the current {@link PAGE} data (usually a {@link com.levelup.http.HttpEngine HttpEngine})
	 * @param nextPageFactory    Factory to get the {@link java.util.concurrent.Callable} to retrieve the next {@link PAGE} data
	 * @param resultCallback     Callback that will be receive the data in the UI thread, the {@link com.levelup.http.async.HttpAsyncCallback#onHttpTaskStarted(com.levelup.http.async.HttpTask)}
	 *                           and {@link com.levelup.http.async.HttpAsyncCallback#onHttpTaskFinished(com.levelup.http.async.HttpTask)} will be called for each page
	 * @param <PAGE>             Type of the Object representing a page
	 */
	public static <PAGE> void readPagesAsync(Callable<PAGE> currentPageRequest, NextPageFactory<PAGE> nextPageFactory,
	                                         HttpAsyncCallback<List<PAGE>> resultCallback) {
		readPagesAsync(currentPageRequest, nextPageFactory, resultCallback, AsyncHttpClient.getExecutor());
	}

	/**
	 * Read all the {@link PAGE} and give the {@code resultCallback} these pages in a {@link List}
	 *
	 * @param currentPageRequest Request to get the current {@link PAGE} data (usually a {@link com.levelup.http.HttpEngine HttpEngine})
	 * @param nextPageFactory    Factory to get the {@link java.util.concurrent.Callable} to retrieve the next {@link PAGE} data
	 * @param resultCallback     Callback that will be receive the data in the UI thread, the {@link com.levelup.http.async.HttpAsyncCallback#onHttpTaskStarted(com.levelup.http.async.HttpTask)}
	 *                           and {@link com.levelup.http.async.HttpAsyncCallback#onHttpTaskFinished(com.levelup.http.async.HttpTask)} will be called for each page
	 * @param executor           {@link java.util.concurrent.Executor} with which each {@link PAGE} data will be retrieved
	 * @param <PAGE>             Type of the Object representing a page
	 */
	public static <PAGE> void readPagesAsync(Callable<PAGE> currentPageRequest, NextPageFactory<PAGE> nextPageFactory,
	                                         HttpAsyncCallback<List<PAGE>> resultCallback, Executor executor) {
		processPagesAsync(currentPageRequest,
				new ArrayList<PAGE>(), new PageCallback<List<PAGE>, PAGE>() {
					@Override
					public void onNewPage(List<PAGE> pagesHolder, PAGE page) {
						pagesHolder.add(page);
					}
				}
				, nextPageFactory
				, resultCallback, executor
		);
	}

	/**
	 * Read all the {@link PAGE} and give the {@code resultCallback} these pages in a {@link PAGE_HOLDER}
	 *
	 * @param currentPageRequest Request to get the current {@link PAGE} data (usually a {@link com.levelup.http.HttpEngine HttpEngine})
	 * @param pagesHolder        Object that will be given the {@link PAGE} data one after the other
	 * @param pageCallback       Callback to handle the loaded {@link PAGE} with the {@link PAGE_HOLDER}, may be {@code null}
	 * @param resultCallback     Callback that will be receive the data in the UI thread, the {@link com.levelup.http.async.HttpAsyncCallback#onHttpTaskStarted(com.levelup.http.async.HttpTask)}
	 *                           and {@link com.levelup.http.async.HttpAsyncCallback#onHttpTaskFinished(com.levelup.http.async.HttpTask)} will be called for each page
	 */
	public static <PAGE_HOLDER, PAGE> void processPagesAsync(Callable<PAGE> currentPageRequest, PAGE_HOLDER pagesHolder, PageCallback<PAGE_HOLDER, PAGE> pageCallback,
	                                                         NextPageFactory<PAGE> nextPageFactory,
	                                                         HttpAsyncCallback<PAGE_HOLDER> resultCallback) {
		processPagesAsync(currentPageRequest, pagesHolder, pageCallback, nextPageFactory, resultCallback, AsyncHttpClient.getExecutor());
	}

	/**
	 * Read all the {@link PAGE} and give the {@code resultCallback} these pages in a {@link PAGE_HOLDER}
	 *
	 * @param currentPageRequest Request to get the current {@link PAGE} data (usually a {@link com.levelup.http.HttpEngine HttpEngine})
	 * @param pagesHolder        Object that will be given the {@link PAGE} data one after the other
	 * @param pageCallback       Callback to handle the loaded {@link PAGE} with the {@link PAGE_HOLDER}, may be {@code null}
	 * @param resultCallback     Callback that will be receive the data in the UI thread, the {@link com.levelup.http.async.HttpAsyncCallback#onHttpTaskStarted(com.levelup.http.async.HttpTask)}
	 *                           and {@link com.levelup.http.async.HttpAsyncCallback#onHttpTaskFinished(com.levelup.http.async.HttpTask)} will be called for each page
	 * @param executor           {@link java.util.concurrent.Executor} with which each {@link PAGE} data will be retrieved
	 */
	public static <PAGE_HOLDER, PAGE> void processPagesAsync(final Callable<PAGE> currentPageRequest, final PAGE_HOLDER pagesHolder, final PageCallback<PAGE_HOLDER, PAGE> pageCallback,
	                                                         final NextPageFactory<PAGE> nextPageFactory,
	                                                         final HttpAsyncCallback<PAGE_HOLDER> resultCallback, final Executor executor) {
		Callable<PAGE_HOLDER> pageCallable = new Callable<PAGE_HOLDER>() {
			@Override
			public PAGE_HOLDER call() throws Exception {
				PAGE page = currentPageRequest.call();
				if (null!=pageCallback)
					pageCallback.onNewPage(pagesHolder, page);
				Callable<PAGE> nextCall = nextPageFactory.createNextPageCallable(page);
				if (nextCall == null)
					return pagesHolder; // no more pages to load, now we return the result for good

				processPagesAsync(nextCall, pagesHolder, pageCallback, nextPageFactory, resultCallback, executor);
				return null;
			}
		};
		HttpTask<PAGE_HOLDER> httpTask = new HttpTask<PAGE_HOLDER>(pageCallable, resultCallback, false);
		executor.execute(httpTask);
	}
}
