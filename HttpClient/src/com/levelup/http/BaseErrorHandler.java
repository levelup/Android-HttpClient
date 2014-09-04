package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import com.levelup.http.parser.ErrorHandlerViaXferTransform;
import com.levelup.http.parser.ParserException;
import com.levelup.http.parser.XferTransformInputStreamString;
import com.levelup.http.parser.XferTransformResponseInputStream;
import com.levelup.http.parser.XferTransformStringJSONObject;

/**
 * Created by robUx4 on 26/08/2014.
 */
public class BaseErrorHandler extends ErrorHandlerViaXferTransform<InputStream> {
	public static final ErrorHandler INSTANCE = new BaseErrorHandler();

	private static final MediaType MediaTypeJSON = MediaType.parse("application/json");

	public BaseErrorHandler() {
		super(XferTransformResponseInputStream.INSTANCE);
	}

	@Override
	public DataErrorException handleErrorData(InputStream errorStream, ImmutableHttpRequest request) throws IOException, ParserException {
		MediaType type = MediaType.parse(request.getHttpResponse().getContentType());
		if (MediaTypeJSON.equalsType(type)) {
			try {
				JSONObject errorData = XferTransformStringJSONObject.INSTANCE.transformData(
						XferTransformInputStreamString.INSTANCE.transformData(errorStream, request)
						, request
				);
				return new DataErrorException(errorData);
			} finally {
				errorStream.close();
			}
		} else if (null == type || "text".equals(type.type())) {
			try {
				String errorData = XferTransformInputStreamString.INSTANCE.transformData(errorStream, request);
				return new DataErrorException(errorData);
			} finally {
				errorStream.close();
			}
		}
		return new DataErrorException(errorStream);
	}
}
