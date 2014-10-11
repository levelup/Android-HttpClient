package co.tophe.body;

import java.io.IOException;
import java.io.OutputStream;

import co.tophe.HttpParameters;
import co.tophe.HttpRequestInfo;
import co.tophe.UploadProgressListener;

/**
 * HTTP parameters suitable to pass to {@link co.tophe.RawHttpRequest}
 */
public interface HttpBodyParameters extends HttpParameters {

	/**
	 * Output stream to write the body of the POST query
	 * @param output Stream to write into
	 * @param request
	 * @param progressListener TODO
	 * @throws IOException
	 */
	void writeBodyTo(OutputStream output, HttpRequestInfo request, UploadProgressListener progressListener) throws IOException;

	/**
	 * Get the Content-Type of the body that will be written.
	 */
	String getContentType();

	/**
	 * Get the length in bytes of the body that will be written
	 */
	long getContentLength();
}
