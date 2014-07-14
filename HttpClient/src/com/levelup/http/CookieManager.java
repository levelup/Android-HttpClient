package com.levelup.http;

import java.io.IOException;


public interface CookieManager {
	public void setCookieHeader(HttpRequest request);
	public void setCookieResponse(ImmutableHttpRequest request) throws IOException;
}
