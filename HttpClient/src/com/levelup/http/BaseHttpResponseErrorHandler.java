package com.levelup.http;

import java.io.IOException;

import org.json.JSONObject;

import com.levelup.http.parser.ResponseToJSONObject;
import com.levelup.http.parser.ResponseToString;

/**
 * Created by robUx4 on 26/08/2014.
 */
public class BaseHttpResponseErrorHandler implements HttpResponseErrorHandler {
	public static final HttpResponseErrorHandler INSTANCE = new BaseHttpResponseErrorHandler();

	public BaseHttpResponseErrorHandler() {
	}

	@Override
	public DataErrorException handleError(HttpResponse response, ImmutableHttpRequest request, Exception cause) {
		// parse the InputStream and handles the error
		MediaType type = MediaType.parse(response.getContentType());
		try {
			if (Util.MediaTypeJSON.equalsType(type)) {
				JSONObject errorData = ResponseToJSONObject.INSTANCE.transformData(response, request);
				return new DataErrorException(errorData, cause);
			} else if (null == type || "text".equals(type.type())) {
				String errorData = ResponseToString.INSTANCE.transformData(response, request);
				return new DataErrorException(errorData, cause);
			}
			return new DataErrorException(response.getContentStream(), cause);
		} catch (IOException ignored) {
			return new DataErrorException(null, cause);
		} catch (ParserException ignored) {
			return new DataErrorException(null, cause);
		}
	}
}
