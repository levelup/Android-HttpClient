package com.levelup.http.body;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.text.TextUtils;

import com.levelup.http.HttpRequestInfo;
import com.levelup.http.UploadProgressListener;

/**
 * HTTP POST parameters encoded as {@code multipart/form-data}
 * <p>Useful to send {@link File} or {@link InputStream}</p>
 */
public class HttpBodyMultiPart implements HttpBodyParameters {
	protected final ArrayList<HttpParam> mParams;

	private static final String charset = "UTF-8";
	private static final String CRLF = "\r\n";
	public static final String boundary = "plUmeMultip4rt";
	private static final String boundarySplit = "--";

	/**
	 * Constructor with an initial amount of parameters to hold
	 * @param capacity amount of parameters the object will get
	 */
	public HttpBodyMultiPart(int capacity) {
		mParams = new ArrayList<HttpParam>(capacity);
	}

	/**
	 * Basic constructor
	 */
	public HttpBodyMultiPart() {
		mParams = new ArrayList<HttpParam>();
	}

	/**
	 * Basic copy constructor
	 * @param copy body to copy parameters from
	 */
	public HttpBodyMultiPart(HttpBodyMultiPart copy) {
		this.mParams = new ArrayList<HttpParam>(copy.mParams);
	}

	/**
	 * Add an {@link InputStream} parameter for the HTTP query
	 * @param name Name of the parameter
	 * @param stream {@link InputStream} to send in the query
	 * @param streamLength the length of the InputStream, must be known in advance
	 * @param contentType Content-Type of the stream or {@code null} if unknown. You may use {@link java.net.URLConnection#guessContentTypeFromStream(InputStream) guessContentTypeFromStream(InputStream)} to determine it.
	 */
	public void addStream(String name, InputStream stream, long streamLength, String contentType) {
		mParams.add(new HttpParam(name, stream, streamLength, contentType));
	}

	/**
	 * Add an {@link InputStream} parameter for the HTTP query, the stream must be seekable to the end
	 * @param name Name of the parameter
	 * @param stream {@link InputStream} to send in the query
	 * @param contentType Content-Type of the stream or {@code null} if unknown. You may use {@link java.net.URLConnection#guessContentTypeFromStream(InputStream) guessContentTypeFromStream(InputStream)} to determine it.
	 */
	public void addStream(String name, InputStream stream, String contentType) {
		if (!stream.markSupported()) throw new IllegalArgumentException("the stream '"+name+"' must be seekable");
		stream.mark(Integer.MAX_VALUE);
		try {
			long streamLength = stream.skip(Integer.MAX_VALUE);
			stream.reset();
			addStream(name, stream, streamLength, contentType);
		} catch (IOException e) {
			throw new IllegalArgumentException("cannot seek to the end of the stream '"+name+"'", e);
		}
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
	public void writeBodyTo(OutputStream output, HttpRequestInfo request, UploadProgressListener progressListener) throws IOException {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new OutputStreamWriter(output, charset), true); // true = autoFlush, important!

			// everything but strings first in the multipart
			for (HttpParam param : mParams)
				if (param.value instanceof File) {
					if (null!=progressListener)
						progressListener.onParamUploadProgress(request, param.name, 0);

					// Send binary file.
					writer.append(boundarySplit).append(boundary).append(CRLF);
					writer.append("Content-Disposition: form-data; name=\"").append(param.name).append("\"; filename=\"").append(((File) param.value).getName()).append('\"').append(CRLF);
					if (!TextUtils.isEmpty(param.contentType))
						writer.append("Content-Type: ").append(param.contentType).append(CRLF);
					writer.append("Content-Transfer-Encoding: binary").append(CRLF);
					writer.append(CRLF).flush();
					InputStream input = null;
					final File file = (File) param.value;
					final long fileLength = file.length();
					long progress = 0;
					try {
						input = new FileInputStream(file);
						byte[] buffer = new byte[1024];
						for (int length; (length = input.read(buffer)) > 0;) {
							output.write(buffer, 0, length);
							progress += length;
							if (null!=progressListener)
								progressListener.onParamUploadProgress(request, param.name, (int)((100 * progress) / fileLength));
						}

						if (null!=progressListener)
							progressListener.onParamUploadProgress(request, param.name, 100);
					} finally {
						if (input != null)
							try {
								input.close();
							} catch (NullPointerException ignored) {
								// okhttp 2.0 bug https://github.com/square/okhttp/issues/690
							} catch (IOException ignored) {
							}
					}
					writer.append(CRLF).flush(); // CRLF is important! It indicates end of binary boundary.
				} else if (param.value instanceof InputStream) {
					if (null!=progressListener)
						progressListener.onParamUploadProgress(request, param.name, 0);

					// Send binary file.
					writer.append(boundarySplit).append(boundary).append(CRLF);
					writer.append("Content-Disposition: form-data; name=\"").append(param.name).append("\"; filename=\"rawstream\"").append(CRLF);
					if (!TextUtils.isEmpty(param.contentType))
						writer.append("Content-Type: ").append(param.contentType).append(CRLF);
					writer.append("Content-Transfer-Encoding: binary").append(CRLF);
					writer.append(CRLF).flush();
					InputStream input = (InputStream) param.value;
					try {
						byte[] buffer = new byte[1024];
						for (int length; (length = input.read(buffer)) > 0;) {
							output.write(buffer, 0, length);
							if (null!=progressListener)
								progressListener.onParamUploadProgress(request, param.name, -1);
						}

						if (null!=progressListener)
							progressListener.onParamUploadProgress(request, param.name, 100);
					} finally {
						try {
							input.close();
						} catch (NullPointerException ignored) {
							// okhttp 2.0 bug https://github.com/square/okhttp/issues/690
						} catch (IOException ignored) {
						}
					}
					writer.append(CRLF).flush(); // CRLF is important! It indicates end of binary boundary.
				}

			// strings last in the multipart in case it fails before
			for (HttpParam param : mParams)
				if (param.value instanceof String) {
					if (null!=progressListener)
						progressListener.onParamUploadProgress(request, param.name, 0);

					// Send text string
					writer.append(boundarySplit).append(boundary).append(CRLF);
					writer.append("Content-Disposition: form-data; name=\"").append(param.name).append("\"").append(CRLF);
					writer.append("Content-Type: ");
					if (TextUtils.isEmpty(param.contentType))
						writer.append("text/plain; charset=" + charset);
					else
						writer.append(param.contentType);
					writer.append(CRLF).append(CRLF);
					writer.append((String) param.value).append(CRLF).flush();

					if (null!=progressListener)
						progressListener.onParamUploadProgress(request, param.name, 100);
				}

			// End of multipart/form-data.
			writer.append(boundarySplit).append(boundary).append(boundarySplit).append(CRLF);
		} finally {
			if (null != writer) {
				writer.close();
			}
		}
	}

	@Override
	public long getContentLength() {
		long contentLength = 0;
		// everything but strings first in the multipart
		for (HttpParam param : mParams)
			if (param.value instanceof File) {
				// Send binary file.
				contentLength += boundarySplit.length();
				contentLength += boundary.length();
				contentLength += CRLF.length();

				contentLength += ("Content-Disposition: form-data; name=\"" + param.name + "\"; filename=\"" + ((File) param.value).getName() + '\"').length();
				contentLength += CRLF.length();

				if (!TextUtils.isEmpty(param.contentType)) {
					contentLength += "Content-Type: ".length();
					contentLength += param.contentType.length();
					contentLength += CRLF.length();
				}
				contentLength += "Content-Transfer-Encoding: binary".length();
				contentLength += CRLF.length();
				contentLength += CRLF.length();

				contentLength += param.length;

				contentLength += CRLF.length(); // CRLF is important! It indicates end of binary boundary.
			} else if (param.value instanceof InputStream) {
				contentLength += boundarySplit.length();
				contentLength += boundary.length();
				contentLength += CRLF.length();

				contentLength += ("Content-Disposition: form-data; name=\"" + param.name + "\"; filename=\"rawstream\"").length();
				contentLength += CRLF.length();

				if (!TextUtils.isEmpty(param.contentType)) {
					contentLength += "Content-Type: ".length();
					contentLength += param.contentType.length();
					contentLength += CRLF.length();
				}
				contentLength += "Content-Transfer-Encoding: binary".length();
				contentLength += CRLF.length();
				contentLength += CRLF.length();

				contentLength += param.length;

				contentLength += CRLF.length(); // CRLF is important! It indicates end of binary boundary.
			}

		// strings last in the multipart in case it fails before
		for (HttpParam param : mParams)
			if (param.value instanceof String) {
				// Send text string
				contentLength += boundarySplit.length();
				contentLength += boundary.length();
				contentLength += CRLF.length();

				if (!TextUtils.isEmpty(param.name)) {
					contentLength += ("Content-Disposition: form-data; name=\"" + param.name + "\"").length();
					contentLength += CRLF.length();
				}
				contentLength += "Content-Type: ".length();
				if (TextUtils.isEmpty(param.contentType)) {
					contentLength += "text/plain; charset=".length();
					contentLength += charset.length();
				} else
					contentLength += param.contentType.length();
				contentLength += CRLF.length();
				contentLength += CRLF.length();

				contentLength += param.length;

				contentLength += CRLF.length();
			}

		// End of multipart/form-data.
		contentLength += boundarySplit.length();
		contentLength += boundary.length();
		contentLength += boundarySplit.length();
		contentLength += CRLF.length();

		return contentLength;
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

	@Override
	public String getContentType() {
		return "multipart/form-data; boundary="+boundary;
	}

	protected static class HttpParam {
		private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";

		public final String name;
		public final Object value;
		public final long length;
		public final String contentType;

		HttpParam(String name, String value) {
			this(name, value, TEXT_PLAIN);
		}

		HttpParam(String name, String value, String contentType) {
			if (null == name) throw new NullPointerException();
			if (null == value) throw new NullPointerException();
			this.name = name;
			this.value = value;
			this.length = value.getBytes().length;
			this.contentType = contentType;
		}

		HttpParam(String name, File value, String contentType) {
			if (null == name) throw new NullPointerException();
			if (null == value) throw new NullPointerException();
			this.name = name;
			this.value = value;
			this.length = value.length();
			this.contentType = contentType;
		}

		HttpParam(String name, InputStream value, long length, String contentType) {
			if (null == name) throw new NullPointerException();
			if (null == value) throw new NullPointerException();
			if (length < 0) throw new IllegalArgumentException("unknown InputStream size to upload");
			this.name = name;
			this.value = value;
			this.length = length;
			this.contentType = contentType;
		}
	}
}
