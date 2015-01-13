package co.tophe;

import android.support.annotation.Nullable;

/**
 * Listener to know the progress of a long HTTP Post (or any request with a body).
 *
 * @see co.tophe.RawHttpRequest#setProgressListener(UploadProgressListener)
 */
public interface UploadProgressListener {
	/**
	 * Notify when the parameter for a request is in progress
	 *
	 * @param request  the request whose progress is notified.
	 * @param name     deprecated, always {@code null}
	 * @param progress 0 when starting, 100 when finished.
	 */
	void onParamUploadProgress(HttpRequestInfo request, @Nullable String name, int progress);
}
