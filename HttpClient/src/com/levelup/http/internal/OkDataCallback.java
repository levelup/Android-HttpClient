package com.levelup.http.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;

public class OkDataCallback implements DataCallback {

	private final ByteBufferList buffer = new ByteBufferList();
	private final long timeout;
	private InputStream is;
	private final AtomicBoolean closed = new AtomicBoolean();
	private AsyncServer asyncserver;

	public OkDataCallback(long timeout, TimeUnit unit) {
		this.timeout = unit.toMillis(timeout);
	}

	@Override
	public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
		synchronized (buffer) {
			this.asyncserver = emitter.getServer();
			if (closed.get())
				asyncserver.stop();
			else {
				if (bb.hasRemaining()) {
					buffer.addAll(bb.getAllArray());
					buffer.notifyAll();
				}
			}
		}
	}

	public void close() throws IOException {
		if (null != is) {
			is.close();
		}
		synchronized (buffer) {
			buffer.recycle();
			buffer.notifyAll();
		}
	}

	public InputStream getInputStream() {
		if (null == is) {
			is = new InputStream() {
				@Override
				public int read(byte[] buf, int byteOffset, int byteCount) throws IOException {
					if (closed.get()) throw new EOFException();
					synchronized (buffer) {
						if (buffer.size() == 0)
							try {
								buffer.wait(timeout);
								//throw new SocketTimeoutException();
							} catch (InterruptedException ignored) {
							}
					}
					if (closed.get()) throw new EOFException();

					if (!buffer.hasRemaining())
						return -1;

					byteCount = Math.min(byteCount, buffer.remaining());
					buffer.get(buf, byteOffset, byteCount);
					return byteCount;
				}

				@Override
				public int read() throws IOException {
					if (closed.get()) throw new EOFException();
					synchronized (buffer) {
						if (buffer.size() == 0)
							try {
								buffer.wait(timeout);
								//throw new SocketTimeoutException();
							} catch (InterruptedException ignored) {
							}
					}
					if (closed.get()) throw new EOFException();

					if (!buffer.hasRemaining())
						return -1;

					return buffer.get();
				}

				@Override
				public int available() throws IOException {
					return buffer.remaining();
				}

				@Override
				public void close() throws IOException {
					synchronized (buffer) {
						closed.set(true);
						if (asyncserver != null)
							asyncserver.stop();
						buffer.notifyAll();
					}
					buffer.recycle();
				}
			};
		}
		return is;
	}
}