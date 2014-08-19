package com.levelup.http.ion.internal;

import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.libcore.RawHeaders;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.loader.HttpLoader;
import com.levelup.http.BaseHttpRequest;

/**
 * A HTTP Loader that sets the received Header on the {@link com.levelup.http.ion.internal.IonAsyncHttpRequest} so it can be
 * checked before parsing the data
 * <p/>
 * Created by robUx4 on 19/08/2014.
 */
public class HttpLoaderWithError extends HttpLoader {
	@Override
	public Future<DataEmitter> load(Ion ion, final AsyncHttpRequest request, final FutureCallback<LoaderEmitter> callback) {
		Future<DataEmitter> result = super.load(ion, request, new FutureCallback<LoaderEmitter>() {
			@Override
			public void onCompleted(Exception e, LoaderEmitter result) {
				if (null != result) {
					IonAsyncHttpRequest ionRequest = (IonAsyncHttpRequest) request;
					ionRequest.engineIon.setHeaders(result.getHeaders());
				}
				callback.onCompleted(e, result);
			}
		});
		return result;
	}

}
