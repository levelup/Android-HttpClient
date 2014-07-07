package com.levelup.http.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okio.AsyncTimeout;
import okio.Buffer;
import okio.Okio;
import okio.Source;
import okio.Timeout;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;

public class OkDataCallback implements DataCallback {

	private final Buffer buffer = new okio.Buffer();
	private final Timeout timeout = new AsyncTimeout();
	private InputStream is;

	public OkDataCallback(long timeout, TimeUnit unit) {
		this.timeout.timeout(timeout, unit);
	}

	@Override
	public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
		synchronized (buffer) {
			byte[] data = bb.getAllByteArray();
			if (null != data && data.length > 0) {
				buffer.write(data);
				((Object) buffer).notifyAll();
			}
		}
	}

	public void close() throws IOException {
		if (null != is) {
			is.close();
		}
		synchronized (buffer) {
			buffer.close();
			((Object) buffer).notifyAll();
		}
	}

	public InputStream getInputStream() {
		if (null == is) {
			final InputStream bufferInputStream = buffer.inputStream();
			is = new InputStream() {
				private final AtomicBoolean closed = new AtomicBoolean();
				@Override
				public int read(byte[] buf, int byteOffset, int byteCount) throws IOException {
					if (closed.get()) throw new EOFException();
					synchronized (buffer) {
						if (buffer.size() == 0)
							try {
								((Object) buffer).wait(timeout.timeoutNanos() / 1000000L);
							} catch (InterruptedException e) {
							}
					}
					if (closed.get()) throw new EOFException();
					return bufferInputStream.read(buf, byteOffset, byteCount);
				}

				@Override
				public int read() throws IOException {
					if (closed.get()) throw new EOFException();
					synchronized (buffer) {
						if (buffer.size() == 0)
							try {
								((Object) buffer).wait(timeout.timeoutNanos() / 1000000L);
							} catch (InterruptedException e) {
							}
					}
					if (closed.get()) throw new EOFException();
					return bufferInputStream.read();
				}

				@Override
				public int available() throws IOException {
					return bufferInputStream.available();
				}

				@Override
				public void close() throws IOException {
					synchronized (buffer) {
						closed.set(true);
						((Object) buffer).notifyAll();
					}
					bufferInputStream.close();
					buffer.close();
				}
			};
		}
		return is;
	}
}