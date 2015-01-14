package co.tophe;

import java.io.IOException;

import android.support.annotation.NonNull;


/**
 * Interface to defined the objects that will handle the HTTP Cookies received/sent.
 */
public interface CookieManager {
	/**
	 * Set the HTTP cookies for this {@link co.tophe.HttpEngine}
	 *
	 * @see co.tophe.HttpEngine#setHeader(String, String)
	 */
	public void setHttpEngineCookies(@NonNull HttpEngine<?, ?> engine);

	/**
	 * Handle the cookies received from an HTTP request.
	 *
	 * @throws IOException if reading the headers from the response fails.
	 */
	public void onCookiesReceived(@NonNull ImmutableHttpRequest request) throws IOException;
}
