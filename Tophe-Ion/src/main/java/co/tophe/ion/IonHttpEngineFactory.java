package co.tophe.ion;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.koushikdutta.ion.Ion;

import co.tophe.HttpEngine;
import co.tophe.HttpEngineFactory;
import co.tophe.HttpResponse;
import co.tophe.ResponseHandler;
import co.tophe.ServerException;
import co.tophe.parser.Utils;
import co.tophe.parser.XferTransform;
import co.tophe.parser.XferTransformChain;
import co.tophe.parser.XferTransformInputStreamHttpStream;

/**
 * An {@link co.tophe.HttpEngineFactory} to create {@link co.tophe.ion.HttpEngineIon} objects for the submitted requests or {@code null}
 * for unsupported requests (like with a live {@link co.tophe.HttpStream HttpStream} output).
 *
 * @author Created by Steve Lhomme on 15/07/2014.
 * @see #getInstance(android.content.Context)
 * @see IonClient#setupIon(com.koushikdutta.ion.Ion)
 */
public class IonHttpEngineFactory implements HttpEngineFactory {

	private static IonHttpEngineFactory INSTANCE;
	//public static final int BOGUS_CONSCRYPT_DUAL_FEEDLY = 6587000; // see https://github.com/koush/ion/issues/443
	//public static final int CONSCRYPT_LACKS_SNI = 6599038; // 6587030 to 6599038 don't have it see https://github.com/koush/ion/issues/428

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
		IonClient.setupIon(ion);
	}

	/**
	 * Get the {@link Ion} instance used by default by TOPHE.
	 */
	@NonNull
	public Ion getDefaultIon() {
		return ion;
	}

	@Nullable
	@Override
	public <T, SE extends ServerException> HttpEngine<T, SE> createEngine(HttpEngine.Builder<T, SE> builder) {
		return createEngine(builder, ion, false);
	}

	/**
	 *
	 * @param builder
	 * @param ion
	 * @param allowBogusSSL Sometimes Ion maybe have problems with SSL, especially with Conscrypt, but you may decide to take the
	 *                         risk anyway and use it in conditions where it may fail
	 * @param <T>
	 * @param <SE>
	 * @return
	 */
	@Nullable
	public <T, SE extends ServerException> HttpEngine<T,SE> createEngine(HttpEngine.Builder<T,SE> builder, Ion ion, boolean allowBogusSSL) {
		if (!allowBogusSSL && IonClient.forbidSSL && "https".equals(builder.getHttpRequest().getUri().getScheme())) {
			return null;
		}

		if (!canHandleXferTransform(builder.getResponseHandler().contentParser))
			return null;

		if (!errorCompatibleWithData(builder.getResponseHandler()))
			// Ion returns the data fully parsed so if we don't have common ground to parse the data and the error data, Ion can't handle the request
			return null;

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
		return Utils.getCommonXferTransform(responseHandler.contentParser, responseHandler.errorParser, false) != null;
	}
}
