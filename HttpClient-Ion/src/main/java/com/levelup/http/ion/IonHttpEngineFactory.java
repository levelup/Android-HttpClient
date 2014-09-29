package com.levelup.http.ion;

import android.content.Context;

import com.koushikdutta.ion.Ion;
import com.levelup.http.DummyHttpEngine;
import com.levelup.http.HttpEngine;
import com.levelup.http.HttpEngineFactory;
import com.levelup.http.HttpResponse;
import com.levelup.http.ResponseHandler;
import com.levelup.http.ServerException;
import com.levelup.http.parser.Utils;
import com.levelup.http.parser.XferTransform;
import com.levelup.http.parser.XferTransformChain;
import com.levelup.http.parser.XferTransformInputStreamHttpStream;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonHttpEngineFactory implements HttpEngineFactory {

	private static IonHttpEngineFactory INSTANCE;

	private final Ion ion;

	public static IonHttpEngineFactory getInstance(Context context) {
		if (null == INSTANCE) {
			INSTANCE = new IonHttpEngineFactory(context);
		}
		return INSTANCE;
	}

	private IonHttpEngineFactory(Context context) {
		if (context == null) {
			throw new NullPointerException("Ion HTTP request with no Context");
		}

		ion = Ion.getDefault(context);
		// until https://github.com/koush/AndroidAsync/issues/210 is fixed
		ion.getConscryptMiddleware().enable(false);
	}

	public Ion getDefaultIon() {
		return ion;
	}

	@Override
	public <T, SE extends ServerException> HttpEngine<T, SE> createEngine(HttpEngine.Builder<T, SE> builder) {
		return createEngine(builder, ion);
	}

	public <T, SE extends ServerException> HttpEngine<T,SE> createEngine(HttpEngine.Builder<T,SE> builder, Ion ion) {
		if (!canHandleXferTransform(builder.getResponseHandler().contentParser))
			return new DummyHttpEngine<T,SE>(builder);

		if (!errorCompatibleWithData(builder.getResponseHandler()))
			// Ion returns the data fully parsed so if we don't have common ground to parse the data and the error data, Ion can't handle the request
			return new DummyHttpEngine<T,SE>(builder);

		return new HttpEngineIon<T,SE>(builder, ion);
	}

	private static <T> boolean canHandleXferTransform(XferTransform<HttpResponse, T> contentParser) {
		if (contentParser instanceof XferTransformChain) {
			XferTransformChain<HttpResponse, T> parser = (XferTransformChain<HttpResponse, T>) contentParser;
			for (XferTransform transform : parser.transforms) {
				if (transform == XferTransformInputStreamHttpStream.INSTANCE)
					return false;
			}
		}
		return true;
	}

	/**
	 * See if we can find common ground to parse the data and the error data inside Ion
	 * @param responseHandler
	 * @return whether Ion will be able to parse the data and the error in its processing thread
	 */
	private static boolean errorCompatibleWithData(ResponseHandler<?,?> responseHandler) {
		return Utils.getCommonXferTransform(responseHandler.contentParser, responseHandler.errorParser) != null;
	}
}
