package co.tophe.parser;

import android.support.annotation.Nullable;

import co.tophe.HttpResponse;
import co.tophe.log.LogManager;

/**
 * Utility functions to handle {@link co.tophe.parser.XferTransform}.
 *
 * @author Created by robUx4 on 28/08/2014.
 */
public class Utils {

	/**
	 * Remove the transforms parts at the beginning of {@code transform} that are also found in {@code commonTransform}.
	 *
	 * @param transform
	 * @param commonTransform
	 * @return {@code null} if all the transformations in {@code transform} were in {@code commonTransform}.
	 */
	@Nullable
	public static XferTransform<Object, Object> skipCommonTransforms(XferTransform<HttpResponse, ?> transform, XferTransform<HttpResponse, ?> commonTransform) {
		if (commonTransform instanceof XferTransformChain) {
			XferTransformChain transforms = (XferTransformChain) transform;
			XferTransformChain commonTransforms = (XferTransformChain) commonTransform;

			for (int i = 0; i < commonTransforms.transforms.length; ++i) {
				transforms = transforms.skipFirstTransform();
			}
			if (transforms.transforms.length == 0)
				return null;
			return transforms;
		}

		if (transform instanceof XferTransformChain) {
			return ((XferTransformChain) transform).skipFirstTransform();
		}
		return null;
	}

	private static XferTransform<HttpResponse, ?> getCommonXferTransform(int transformLength, XferTransformChain<HttpResponse, ?> t1, XferTransformChain<?, ?> t2) {
		if (transformLength <= 0)
			return null;

		if (!t1.transforms[transformLength - 1].equals(t2.transforms[transformLength - 1])) {
			return getCommonXferTransform(transformLength - 1, t1, t2);
		}

		if (transformLength == 1)
			return t1.transforms[0];

		while (transformLength == t1.transforms.length) {
			t1 = t1.removeLastTransform();
		}
		return t1;
	}

	/**
	 * Get the common transformations found at the beginning of {@code t1} and {@code t2}.
	 *
	 * @param t1
	 * @param t2
	 * @param logErrors {@code true} if you want to log errors via {@link co.tophe.log.LogManager}.
	 * @return {@code null} if there is no common transformation between {@code t1} and {@code t2}.
	 */
	@Nullable
	public static XferTransform<HttpResponse, ?> getCommonXferTransform(XferTransform<HttpResponse, ?> t1, XferTransform<HttpResponse, ?> t2, boolean logErrors) {
		if (t1.equals(t2)) {
			return t1;
		}

		if (t1 instanceof XferTransformChain) {
			XferTransformChain<HttpResponse, ?> dataTransforms = (XferTransformChain<HttpResponse, ?>) t1;

			if (!(t2 instanceof XferTransformChain)) {
				if (t2.equals(dataTransforms.transforms[0])) {
					return t2;
				}

				if (logErrors) LogManager.getLogger().w("Error parser:" + t2 + " not compatible with data parser:" + t1);
				return null;
			}

			XferTransformChain<HttpResponse, ?> errorTransforms = (XferTransformChain<HttpResponse, ?>) t2;
			return getCommonXferTransform(Math.min(dataTransforms.transforms.length, errorTransforms.transforms.length), dataTransforms, errorTransforms);
		}

		if (t2 instanceof XferTransformChain) {
			XferTransformChain<HttpResponse, ?> errorTransforms = (XferTransformChain<HttpResponse, ?>) t2;

			if (t1.equals(errorTransforms.transforms[0])) {
				return t1;
			}

			if (logErrors) LogManager.getLogger().w("Error parser:" + t2 + " not compatible with data parser:" + t1);
			return null;
		}

		if (logErrors) LogManager.getLogger().w("Error parser:" + t2 + " not compatible with data parser:" + t1);
		return null;
	}
}
