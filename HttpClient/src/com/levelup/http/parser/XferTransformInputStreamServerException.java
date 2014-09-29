package com.levelup.http.parser;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.MediaType;
import com.levelup.http.ServerException;

/**
 * Created by robUx4 on 29/09/2014.
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
		return new ServerException.Builder(request, errorData).build();
	}
}
