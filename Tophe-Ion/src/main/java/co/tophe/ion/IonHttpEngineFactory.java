package co.tophe.ion;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.koushikdutta.ion.Ion;
import co.tophe.DummyHttpEngine;
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
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonHttpEngineFactory implements HttpEngineFactory {

	private static IonHttpEngineFactory INSTANCE;
	public static final int PLAY_SERVICES_BOGUS_CONSCRYPT = 5089030;

	private final Ion ion;

	public static IonHttpEngineFactory getInstance(Context context) {
		if (null == INSTANCE) {
			INSTANCE = new IonHttpEngineFactory(context);
		}
		return INSTANCE;
	}

	private IonHttpEngineFactory(@NonNull Context context) {
		if (context == null) {
			throw new NullPointerException("Ion HTTP request with no Context");
		}

		ion = Ion.getDefault(context);
		setupIon(ion);
	}

	private static Boolean useConscrypt;

	public static void setupIon(Ion ion) {
		if (null==useConscrypt) {
			useConscrypt = true;
			// The Play Services are are bogus on old versions, see https://android-review.googlesource.com/#/c/99698/
			// disable conscrypt until https://github.com/koush/AndroidAsync/issues/210 passes
			PackageManager pm = ion.getContext().getPackageManager();
			try {
				PackageInfo pI = pm.getPackageInfo("com.google.android.gms", 0);
				if (pI != null) {
					useConscrypt = pI.versionCode > PLAY_SERVICES_BOGUS_CONSCRYPT;
				}
			} catch (PackageManager.NameNotFoundException ignored) {
			}
		}

		ion.getConscryptMiddleware().enable(useConscrypt);
	}

	@NonNull
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
		return Utils.getCommonXferTransform(responseHandler.contentParser, responseHandler.errorParser, false) != null;
	}
}
