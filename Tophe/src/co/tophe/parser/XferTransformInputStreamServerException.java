package co.tophe.parser;

import java.io.IOException;
import java.io.InputStream;

import co.tophe.ImmutableHttpRequest;
import co.tophe.MediaType;
import co.tophe.ServerException;

/**
 * @author Created by robUx4 on 29/09/2014.
 */
public class XferTransformInputStreamServerException implements XferTransform<InputStream, ServerException> {
	public static final XferTransformInputStreamServerException INSTANCE = new XferTransformInputStreamServerException();

	public static final MediaType MediaTypeJSON = MediaType.parse("application/json");

	private XferTransformInputStreamServerException() {
	}

	@Override
	public ServerException transformData(InputStream errorStream, ImmutableHttpRequest request) throws IOException, ParserException {
		Object errorData = null;
		MediaType type = MediaType.parse(request.getHttpResponse().getContentType());
		if (MediaTypeJSON.equalsType(type)) {
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
