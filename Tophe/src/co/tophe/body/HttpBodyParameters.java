package co.tophe.body;

import java.io.IOException;
import java.io.OutputStream;

import co.tophe.HttpParameters;
import co.tophe.HttpRequestInfo;
import co.tophe.UploadProgressListener;

/**
 * Interface to define the HTTP body sent with an HTTP request.
 *
 * @see co.tophe.RawHttpRequest.Builder#setBody(HttpBodyParameters)
 * @see co.tophe.RawHttpRequest.Builder#setBody(String, HttpBodyParameters)
 * @see co.tophe.body.HttpBodyMultiPart
 * @see co.tophe.body.HttpBodyUrlEncoded
 * @see co.tophe.body.HttpBodyJSON
 * @see co.tophe.body.HttpBodyString
 */
public interface HttpBodyParameters extends HttpParameters {

	/**
	 * Output stream to write the body of the POST query
	 *
	 * @param output           Stream to write into
	 * @param request          the request the OutputStream belongs to
	 * @param progressListener the {@link co.tophe.UploadProgressListener} to notify when data are uploaded
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
