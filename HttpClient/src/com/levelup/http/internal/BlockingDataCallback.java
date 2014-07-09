package com.levelup.http.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.levelup.http.LogManager;

public class BlockingDataCallback implements DataCallback {

	//private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE;
	private static final int MAX_BUFFER_SIZE = 16 * 1024;
	private boolean DEBUG = true;

	private final ReentrantLock bufferLock;
	private final Condition notEmpty;
	private final Condition notFull;

	/**
	 * The buffer containing the data received from the network, protected by {@link #bufferLock}
	 */
	private final ByteBufferList buffer = new ByteBufferList();
	private final long timeout;
	private final TimeUnit timeoutUnit;
	private InputStream is;
	private final AtomicBoolean closed = new AtomicBoolean();

	/**
	 * The server feeding us the data, protected by {@link #bufferLock}
	 */
	private AsyncServer asyncServer;
	/**
	 * Indicates the {@link #asyncServer} has been stopped after a call to {@link #close()}, protected by {@link #bufferLock}
	 */
	private boolean asyncServerStopped;

	public BlockingDataCallback(long timeout, TimeUnit unit) {
		this.timeout = timeout;
		this.timeoutUnit = unit;
		bufferLock = new ReentrantLock();
		notEmpty = bufferLock.newCondition();
		notFull = bufferLock.newCondition();
	}

	@Override
	public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
		if (DEBUG) LogManager.getLogger().d(this + " new data arrived:"+bb.remaining());
		bufferLock.lock();
		try {
			this.asyncServer = emitter.getServer();

			if (closed.get()) {
				if (!asyncServerStopped) {
					asyncServerStopped = true;
					asyncServer.stop();
				}
				bb.recycle(); // consume everything
				return;
			}


			while (bb.hasRemaining()) {
				if (buffer.size() >= MAX_BUFFER_SIZE) {
					if (DEBUG) LogManager.getLogger().d(this + " waiting for space available");
					notFull.await();
				}

				if (closed.get()) {
					if (!asyncServerStopped) {
						asyncServerStopped = true;
						asyncServer.stop();
					}
					bb.recycle(); // consume everything
					return;
				}

				int amountToRead = Math.min(MAX_BUFFER_SIZE - buffer.size(), bb.remaining());
				bb.get(buffer, amountToRead);
				notEmpty.signal();
			}
		} catch (InterruptedException e) {
			bb.recycle(); // consume everything and leave the loop
		} finally {
			bufferLock.unlock();
		}
	}

	public void close() throws IOException {
		if (DEBUG) LogManager.getLogger().d(this + " closing");
		if (!closed.getAndSet(true)) {
			if (null != is) {
				is.close();
			}

			bufferLock.lock();
			try {
				if (null != asyncServer && !asyncServerStopped) {
					asyncServerStopped = true;
					asyncServer.stop();
				}
				buffer.recycle();
				notFull.signal(); // unblock the feeder if it's blocked
			} finally {
				bufferLock.unlock();
			}
		}
	}

	public InputStream getInputStream() {
		if (null == is) {
			is = new InputStream() {
				@Override
				public int read(byte[] buf, int byteOffset, int byteCount) throws IOException {
					if (closed.get()) throw new EOFException();

					if (byteCount<=0)
						return byteCount;

					bufferLock.lock();
					try {
						if (!buffer.hasRemaining()) {
							if (DEBUG) LogManager.getLogger().d(BlockingDataCallback.this + " waiting for new data");
							notEmpty.await(timeout, timeoutUnit);
						}

						if (!buffer.hasRemaining())
							return -1;

						byteCount = Math.min(byteCount, buffer.remaining());
						buffer.get(buf, byteOffset, byteCount);
						
						notFull.signal();
						
						return byteCount;
					} catch (InterruptedException e) {
						return -1;
					} finally {
						bufferLock.unlock();
					}
				}

				@Override
				public int read() throws IOException {
					if (closed.get()) throw new EOFException();

					bufferLock.lock();
					try {
						if (!buffer.hasRemaining()) {
							if (DEBUG) LogManager.getLogger().d(BlockingDataCallback.this + " waiting for new data");
							notEmpty.await(timeout, timeoutUnit);
						}

						if (!buffer.hasRemaining())
							return -1;

						notFull.signal();

						return buffer.get();
					} catch (InterruptedException e) {
						return -1;
					} finally {
						bufferLock.unlock();
					}
				}

				@Override
				public int available() throws IOException {
					bufferLock.lock();
					try {
						return buffer.remaining();
					} finally {
						bufferLock.unlock();
					}
				}

				private boolean reentrantClose;
				@Override
				public void close() throws IOException {
					if (!reentrantClose) {
						reentrantClose = true;
						BlockingDataCallback.this.close();
						reentrantClose = false;
					}
				}
			};
		}
		return is;
	}
}