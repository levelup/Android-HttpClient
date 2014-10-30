package co.tophe.body;

import java.io.IOException;
import java.io.OutputStream;

import android.support.annotation.NonNull;

import co.tophe.HttpRequestInfo;
import co.tophe.UploadProgressListener;


/**
 * HTTP body class that consists of a String data and its Content-Type 
 */
public class HttpBodyString implements HttpBodyParameters {

	protected final String value;
	private final String contentType;

	public HttpBodyString(String value, String contentType) {
		this.value = value;
		this.contentType = contentType;
	}

	public HttpBodyString(HttpBodyString copy) {
		this(copy.value, copy.contentType);
	}

	/**
	 * Do not use, extra parameters in the URL are not supported
	 * @throws IllegalAccessError
	 */
	@Deprecated
	@Override
	public void add(@NonNull String name, String value) {
		throw new IllegalAccessError();
	}

	/**
	 * Do not use, extra parameters in the URL are not supported
	 * @throws IllegalAccessError
	 */
	@Deprecated
	@Override
	public void add(@NonNull String name, boolean b) {
		throw new IllegalAccessError();
	}

	/**
	 * Do not use, extra parameters in the URL are not supported
	 * @throws IllegalAccessError
	 */
	@Deprecated
	@Override
	public void add(@NonNull String name, int value) {
		throw new IllegalAccessError();
	}

	/**
	 * Do not use, extra parameters in the URL are not supported
	 * @throws IllegalAccessError
	 */
	@Deprecated
	@Override
	public void add(@NonNull String name, long value) {
		throw new IllegalAccessError();
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public long getContentLength() {
		return value.getBytes().length;
	}

	@Override
	public void writeBodyTo(OutputStream output, HttpRequestInfo request, UploadProgressListener progressListener) throws IOException {
		output.write(value.getBytes());
	}
}
