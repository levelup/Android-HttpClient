package com.levelup.http.internal;

import org.json.JSONObject;

import com.levelup.http.HttpException;

/**
 * Created by Steve Lhomme on 11/07/2014.
 */
public interface HttpErrorHandler {
	HttpException.Builder newException();
}
