package co.tophe.parser;

import java.io.IOException;
import java.io.InputStream;

import co.tophe.ImmutableHttpRequest;
import co.tophe.MediaType;
import co.tophe.ServerException;

/**
 * Helper class to transform an {@link java.io.InputStream} into a raw/untyped {@link co.tophe.ServerException}.
 * <p>If a JSON response is detected, the object returned by {@link co.tophe.ServerException#getServerError()} is a {@link org.json.JSONObject}.</p>
 * <p>Includes a static {@link #INSTANCE} for convenience.</p>
 *
 * @author Created by robUx4 on 29/09/2014.
 */
public class XferTransformInputStreamServerException implements XferTransform<InputStream, ServerException> {
	/**
	 * The instance you should use when you want to get a {@link co.tophe.ServerException} from an {@link java.io.InputStream}.
	 * <p>If a JSON response is detected, the object returned by {@link co.tophe.ServerException#getServerError()} is a {@link org.json.JSONObject}.</p>
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setContentParser(XferTransform) BaseHttpRequest.Builder.setContentParser()
	 */
	public static final XferTransformInputStreamServerException INSTANCE = new XferTransformInputStreamServerException();

	/**
	 * The {@link co.tophe.MediaType} to describe a JSON MIME type.
	 */
	public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

	private XferTransformInputStreamServerException() {
	}

	@Override
	public ServerException transformData(InputStream errorStream, ImmutableHttpRequest request) throws IOException, ParserException {
		Object errorData = null;
		MediaType type = MediaType.parse(request.getHttpResponse().getContentType());
		if (MEDIA_TYPE_JSON.equalsType(type)) {
			try {
				errorData = XferTransformStringJSONObject.INSTANCE.transformData(
						XferTransformInputStreamString.INSTANCE.transformData(errorStream, request)
						, request
				);
			} finally {
				errorStream.close();
			}
		} else if (null == type || "text".equals(type.type())) {
			try {
				errorData = XferTransformInputStreamString.INSTANCE.transformData(errorStream, request);
			} finally {
				errorStream.close();
			}
		} else {
			errorData = errorStream;
		}
		return new ServerException(request, errorData);
	}
}
