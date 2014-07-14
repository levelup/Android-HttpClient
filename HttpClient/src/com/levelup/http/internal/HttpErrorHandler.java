package com.levelup.http.internal;

import org.json.JSONObject;

import com.levelup.http.HttpException;

/**
 * Created by Steve Lhomme on 11/07/2014.
 */
public interface HttpErrorHandler {
	/**
	 * Handle error data returned in JSON format
	 *
	 * @param builder
	 * @param jsonData
	 * @return
	 */
	HttpException.Builder handleJSONError(HttpException.Builder builder, JSONObject jsonData);

	HttpException.Builder newException(BaseHttpRequestImpl httpRequest);
}
