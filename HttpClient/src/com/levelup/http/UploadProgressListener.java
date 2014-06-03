package com.levelup.http;

public interface UploadProgressListener {
	/**
	 * Notify when the parameter for a request is in progress
	 * @param request 
	 * @param name Name of the parameter, null when the whole upload starts/ends
	 * @param progress 0 when starting, 100 when finished, -1 when uploading without a known size
	 */
	void onParamUploadProgress(BaseHttpRequest<?> request, String name, int progress);
}
