package com.levelup.http;

import java.io.IOException;


public interface CookieManager {
	public void setHttpEngineCookies(HttpEngine<?,?> engine);
	public void onCookiesReceived(ImmutableHttpRequest request) throws IOException;
}
