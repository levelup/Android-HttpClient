package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;

import com.koushikdutta.ion.builder.Builders;

/**
 * HTTP parameters suitable to pass to {@link BaseHttpRequest} 
 */
public interface HttpBodyParameters extends HttpParameters {

	/**
	 * Set the body properties on the Ion request builder
	 * @param requestBuilder Ion request builder to set the body on
	 */
	void setOutputData(Builders.Any.B requestBuilder);

	/**
	 * Output stream to write the body of the POST query
	 * @param output Stream to write into
	 * @param request 
	 * @param progressListener TODO
	 * @throws IOException
	 */
	void writeBodyTo(OutputStream output, HttpRequest request, UploadProgressListener progressListener) throws IOException;

	/**
	 * Get the Content-Type of the body that will be written.
	 */
	String getContentType();

	/**
	 * Get the length in bytes of the body that will be written
	 */
	long getContentLength();
}
