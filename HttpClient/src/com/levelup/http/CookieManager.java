package com.levelup.http;

import java.io.IOException;

import com.koushikdutta.ion.Response;


public interface CookieManager {
	public void setCookieHeader(HttpRequest request);
	public void setCookieResponse(HttpRequest request, Response<?> resp) throws IOException;
}
