package com.levelup.http;

public interface UploadProgressListener {
	/**
	 * Notify when the parameter for a request is in progress
	 * @param request
	 * @param name deprecated, always {@code null}
	 * @param progress 0 when starting, 100 when finished
	 */
	void onParamUploadProgress(HttpRequestInfo request, String name, int progress);
}
