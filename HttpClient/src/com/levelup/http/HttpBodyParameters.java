package com.levelup.http;

import java.io.IOException;
import java.io.OutputStream;

import com.koushikdutta.ion.builder.Builders;

/**
 * HTTP parameters suitable to pass to {@link BaseHttpRequest} 
 */
public interface HttpBodyParameters extends HttpParameters {
	
	/**
	 * Output stream to write the body of the POST query
	 * @param output Stream to write into
	 * @param request 
	 * @param progressListener TODO
	 * @throws IOException
	 */
	void writeBodyTo(OutputStream output, BaseHttpRequest<?> request, UploadProgressListener progressListener) throws IOException;

	void setOutputData(Builders.Any.B requestBuilder);
}
