package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import com.levelup.http.parser.HttpFailureHandlerViaXferTransform;
import com.levelup.http.parser.ParserException;
import com.levelup.http.parser.XferTransformInputStreamString;
import com.levelup.http.parser.XferTransformResponseInputStream;
import com.levelup.http.parser.XferTransformStringJSONObject;

/**
 * Created by robUx4 on 26/08/2014.
 */
public class BaseHttpFailureHandler extends HttpFailureHandlerViaXferTransform<InputStream> {
	public static final HttpFailureHandler INSTANCE = new BaseHttpFailureHandler();

	public static final MediaType MediaTypeJSON = MediaType.parse("application/json");

	public BaseHttpFailureHandler() {
		super(XferTransformResponseInputStream.INSTANCE);
	}

	@Override
	public HttpFailureException handleErrorData(InputStream errorStream, ImmutableHttpRequest request) throws IOException, ParserException {
		HttpFailure httpFailure = null;
		MediaType type = MediaType.parse(request.getHttpResponse().getContentType());
		if (MediaTypeJSON.equalsType(type)) {
			try {
				JSONObject errorData = XferTransformStringJSONObject.INSTANCE.transformData(
						XferTransformInputStreamString.INSTANCE.transformData(errorStream, request)
						, request
				);
				httpFailure = new HttpFailure(errorData);
			} finally {
				errorStream.close();
			}
		} else if (null == type || "text".equals(type.type())) {
			try {
				String errorData = XferTransformInputStreamString.INSTANCE.transformData(errorStream, request);
				httpFailure = new HttpFailure(errorData);
			} finally {
				errorStream.close();
			}
		} else {
			httpFailure = new HttpFailure(errorStream);
		}
		return new HttpFailureException.Builder(request, httpFailure).build();
	}
}
