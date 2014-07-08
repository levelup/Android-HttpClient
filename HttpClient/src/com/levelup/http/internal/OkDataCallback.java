package com.levelup.http.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;

import okio.AsyncTimeout;
import okio.Buffer;
import okio.Timeout;

public class OkDataCallback implements DataCallback {

	private final Buffer buffer = new okio.Buffer();
	private final Timeout timeout = new AsyncTimeout();
	private InputStream is;
	private final AtomicBoolean closed = new AtomicBoolean();
	private AsyncServer asyncserver;

	public OkDataCallback(long timeout, TimeUnit unit) {
		this.timeout.timeout(timeout, unit);
	}

	@Override
	public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
		synchronized (buffer) {
			this.asyncserver = emitter.getServer();
			if (closed.get())
				asyncserver.stop();
			else {
				byte[] data = bb.getAllByteArray();
				if (null != data && data.length > 0) {
					buffer.write(data);
					((Object) buffer).notifyAll();
				}
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
				@Override
				public int read(byte[] buf, int byteOffset, int byteCount) throws IOException {
					if (closed.get()) throw new EOFException();
					synchronized (buffer) {
						if (buffer.size() == 0)
							try {
								((Object) buffer).wait(timeout.timeoutNanos() / 1000000L);
								//throw new SocketTimeoutException();
							} catch (InterruptedException ignored) {
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
								//throw new SocketTimeoutException();
							} catch (InterruptedException ignored) {
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
						if (asyncserver!=null)
							asyncserver.stop();
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