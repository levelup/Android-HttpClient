package com.levelup.http.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okio.AsyncTimeout;
import okio.Buffer;
import okio.Okio;
import okio.Source;
import okio.Timeout;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;

public class OkDataCallback implements DataCallback, Source {

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

	@Override
	public long read(Buffer sink, long byteCount) throws IOException {
		synchronized (buffer) {
			if (buffer.size() == 0)
				try {
					((Object) buffer).wait(timeout.timeoutNanos() / 1000000L);
				} catch (InterruptedException e) {
				}
		}
		return buffer.read(sink, byteCount);
	}

	@Override
	public Timeout timeout() {
		return timeout;
	}

	@Override
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
			//Source tb = timeout.source(this);
			//is = Okio.buffer(tb).inputStream();
			is = buffer.inputStream();
		}
		return is;
	}
}