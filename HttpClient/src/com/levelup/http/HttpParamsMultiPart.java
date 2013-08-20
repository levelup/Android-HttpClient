package com.levelup.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import org.apache.http.protocol.HTTP;

import android.text.TextUtils;

/**
 * HTTP POST parameters encoded as {@code multipart/form-data}
 * <p>Useful to send {@link File} or {@link InputStream}</p> 
 */
public class HttpParamsMultiPart implements HttpPostParameters {
	private final ArrayList<HttpParam> mParams;

	protected static final String charset = "UTF-8";
	private static final String CRLF = "\r\n";
	private static final String boundary = "plUmeMultip4rt";
	private static final String boundarySplit = "--";

	/**
	 * Constructor with an initial amount of parameters to hold
	 * @param capacity amount of parameters the object will get
	 */
	public HttpParamsMultiPart(int capacity) {
		mParams = new ArrayList<HttpParam>(capacity);
	}

	/**
	 * Basic constructor
	 */
	public HttpParamsMultiPart() {
		mParams = new ArrayList<HttpParam>();
	}

	/**
	 * Add an {@link InputStream} parameter for the HTTP query
	 * @param name Name of the parameter
	 * @param stream {@link InputStream} to send in the query
	 * @param contentType Content-Type of the stream or {@code null} if unknown. You may use {@link java.net.URLConnection#guessContentTypeFromStream(InputStream) guessContentTypeFromStream(InputStream)} to determine it.
	 */
	public void addStream(String name, InputStream stream, String contentType) {
		mParams.add(new HttpParam(name, stream, contentType));
	}

	/**
	 * Add a {@link File} parameter for the HTTP query
	 * @param name Name of the parameter
	 * @param file File to send in the query
	 * @param contentType Content-Type of the file or {@code null} if unknown. You may use {@link java.net.URLConnection#guessContentTypeFromName(String) guessContentTypeFromName(String)} to determine it.
	 */
	public void addFile(String name, File file, String contentType) {
		mParams.add(new HttpParam(name, file, contentType));
	}

	@Override
	public void setRequestProperties(HttpURLConnection connection) {
		connection.setRequestProperty(HTTP.CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);
	}

	@Override
	public void writeBodyTo(OutputStream output) throws IOException {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new OutputStreamWriter(output, charset), true); // true = autoFlush, important!

			// everything but strings first in the multipart
			for (HttpParam param : mParams)
				if (param.value instanceof File) {
					// Send binary file.
					writer.append(boundarySplit).append(boundary).append(CRLF);
					writer.append("Content-Disposition: form-data; name=\""+param.name+"\"; filename=\"" + ((File) param.value).getName() + '\"').append(CRLF);
					if (TextUtils.isEmpty(param.contentType))
						writer.append("Content-Type: ").append(param.contentType).append(CRLF);
					writer.append("Content-Transfer-Encoding: binary").append(CRLF);
					writer.append(CRLF).flush();
					InputStream input = null;
					try {
						input = new FileInputStream(((File) param.value));
						byte[] buffer = new byte[1024];
						for (int length = 0; (length = input.read(buffer)) > 0;) {
							output.write(buffer, 0, length);
						}
					} finally {
						if (input != null)
							try {
								input.close(); 
							} catch (IOException ignored) {
							}
					}
					writer.append(CRLF).flush(); // CRLF is important! It indicates end of binary boundary.
				} else if (param.value instanceof InputStream) {
					// Send binary file.
					writer.append(boundarySplit).append(boundary).append(CRLF);
					writer.append("Content-Disposition: form-data; name=\""+param.name+"\"; filename=\"rawstream\"").append(CRLF);
					if (TextUtils.isEmpty(param.contentType))
						writer.append("Content-Type: ").append(param.contentType).append(CRLF);
					writer.append("Content-Transfer-Encoding: binary").append(CRLF);
					writer.append(CRLF).flush();
					InputStream input = (InputStream) param.value;
					try {
						byte[] buffer = new byte[1024];
						for (int length = 0; (length = input.read(buffer)) > 0;) {
							output.write(buffer, 0, length);
						}
					} finally {
						if (input != null)
							try {
								input.close();
							} catch (IOException ignored) {
							}
					}
					writer.append(CRLF).flush(); // CRLF is important! It indicates end of binary boundary.
				}

			// strings last in the multipart in case it fails before
			for (HttpParam param : mParams)
				if (param.value instanceof String) {
					// Send text string
					writer.append(boundarySplit).append(boundary).append(CRLF);
					if (!TextUtils.isEmpty(param.name))
						writer.append("Content-Disposition: form-data; name=\""+param.name+"\"").append(CRLF);
					writer.append("Content-Type: ");
					if (TextUtils.isEmpty(param.contentType))
						writer.append("text/plain; charset=" + charset);
					else
						writer.append(param.contentType);
					writer.append(CRLF).append(CRLF);
					writer.append((String) param.value).append(CRLF).flush();
				}

			// End of multipart/form-data.
			writer.append(boundarySplit).append(boundary).append(boundarySplit).append(CRLF);
		} finally {
			if (null != writer) {
				writer.close();
				writer = null;
			}
		}
	}

	@Override
	public void add(String name, String value) {
		mParams.add(new HttpParam(name, value));
	}

	/**
	 * Add a String parameter for the HTTP query with a specific content type
	 * @param name Name of the parameter
	 * @param value Value of the parameter
	 * @param contentType Content-Type of the String value
	 */
	public void add(String name, String value, String contentType) {
		mParams.add(new HttpParam(name, value, contentType));
	}

	@Override
	public void add(String name, boolean b) {
		add(name, String.valueOf(b));
	}

	@Override
	public void add(String name, int i) {
		add(name, Integer.toString(i));
	}

	@Override
	public void add(String name, long l) {
		add(name, Long.toString(l));
	}

	private static class HttpParam {
		private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";

		private final String name;
		private final Object value;
		private final String contentType;

		HttpParam(String name, String value) {
			this(name, value, TEXT_PLAIN);
		}

		HttpParam(String name, String value, String contentType) {
			if (null == name) throw new NullPointerException();
			if (null == value) throw new NullPointerException();
			this.name = name;
			this.value = value;
			this.contentType = contentType;
		}

		HttpParam(String name, File value, String contentType) {
			if (null == name) throw new NullPointerException();
			if (null == value) throw new NullPointerException();
			this.name = name;
			this.value = value;
			this.contentType = contentType;
		}

		HttpParam(String name, InputStream value, String contentType) {
			if (null == name) throw new NullPointerException();
			if (null == value) throw new NullPointerException();
			this.name = name;
			this.value = value;
			this.contentType = contentType;
		}
	}
}
