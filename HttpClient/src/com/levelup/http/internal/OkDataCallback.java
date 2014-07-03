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
	private InputStream is;

	@Override
	public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
		synchronized (buffer) {
			byte[] data = bb.getAllByteArray();
			if (null!=data && data.length > 0) {
				buffer.write(data);
				buffer.notifyAll();
			}
		}
	}

	@Override
	public long read(Buffer sink, long byteCount) throws IOException {
		synchronized (buffer) {
			if (buffer.size()==0)
				try {
					buffer.wait(90 * 1000); // TODO it should be configurable for the request
				} catch (InterruptedException e) {
				}
		}
		return buffer.read(sink, byteCount);
	}

	@Override
	public Timeout timeout() {
		return Timeout.NONE;
	}

	@Override
	public void close() throws IOException {
		if (null!=is) {
			is.close();
		}
		synchronized (buffer) {
			buffer.close();
			buffer.notifyAll();
		}
	}

	public InputStream getInputStream() {
		if (null==is) {
			AsyncTimeout timeout = new AsyncTimeout();
			timeout.timeout(90, TimeUnit.SECONDS); // TODO it should be configurable for the request
			Source tb = timeout.source(this);
			is = Okio.buffer(tb).inputStream();
		}
		return is;
	}
}