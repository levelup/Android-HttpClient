package com.levelup.http.signed.oauth1;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.BaseHttpRequest;
import com.levelup.http.HttpClient;
import com.levelup.http.HttpException;
import com.levelup.http.HttpRequestPost;
import com.levelup.http.TypedHttpRequest;
import com.levelup.http.signed.OAuthClientApp;
import com.levelup.http.signed.oauth1.internal.ResponseAdapter;

import oauth.signpost.OAuth;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.http.HttpParameters;
import oauth.signpost.http.HttpRequest;
import oauth.signpost.http.HttpResponse;

/**
 * Helper class to retrieve the <a
 * href="http://tools.ietf.org/html/rfc5849#page-4">Request Token</a> (temporary token) and <a
 * href="http://tools.ietf.org/html/rfc5849#page-4">Access Token</a> (user token) from an
 * <a href="http://tools.ietf.org/html/rfc5849">OAuth 1.0</a> service for the specified {@link OAuthClientApp}
 */
public class HttpClientOAuth1Provider {

	private final HttpClientOAuth1Consumer consumer;
	private final DefaultOAuthProvider provider;

	/**
	 * Base constructor to retrieve the tokens for the given client app.
	 * 
	 * @see #retrieveRequestToken(String, String...)
	 * @see #retrieveAccessToken(String, String...)
	 * @param clientApp The {@link OAuthClientApp} whose keys will be used to retrieve the token
	 * @param requestTokenUrl The URL of the service provider to get a Request token
	 * @param accessTokenUrl The URL of the service provider to get an Access token
	 * @param authorizationWebsiteUrl The URL of the service provider to authorize your website
	 */
	public HttpClientOAuth1Provider(OAuthClientApp clientApp, String requestTokenUrl, String accessTokenUrl, String authorizationWebsiteUrl) {
		this(new OAuth1ConsumerClocked(clientApp), requestTokenUrl, accessTokenUrl, authorizationWebsiteUrl);
	}

	/**
	 * Constructor to retrieve the tokens for the given client app with a custom {@link HttpClientOAuth1Consumer} and {@link ProviderHttpRequestFactory}
	 * <p>The {@code requestFactory} can do special processing on the response via {@link BaseHttpRequest#setResponse(com.levelup.http.HttpResponse) HttpRequest.setResponse}
	 * 
	 * @param consumer The consumer used to retrieve the tokens
	 * @param requestTokenUrl The URL of the service provider to get a Request token
	 * @param accessTokenUrl The URL of the service provider to get an Access token
	 * @param authorizationWebsiteUrl The URL of the service provider to authorize your website
	 */
	public HttpClientOAuth1Provider(HttpClientOAuth1Consumer consumer, String requestTokenUrl, String accessTokenUrl, String authorizationWebsiteUrl) {
		this.consumer = consumer;
		this.provider = new DefaultOAuthProvider(requestTokenUrl, accessTokenUrl, authorizationWebsiteUrl) {
			private static final long serialVersionUID = 8102585589144551017L;

			@Override
			protected HttpRequest createRequest(String endpointUrl) throws IOException {
				final TypedHttpRequest<?> request;
				if (HttpClientOAuth1Provider.this.consumer instanceof OAuth1ConsumerClocked) {
					OAuth1ConsumerClocked cons = (OAuth1ConsumerClocked) HttpClientOAuth1Provider.this.consumer;
					request = cons.createRequest(endpointUrl);
				} else {
					request = new HttpRequestPost<Void>(endpointUrl, null);
				}
				return new OAuth1RequestAdapter(request);
			}

			@Override
			protected HttpResponse sendRequest(HttpRequest request) throws IOException {
				TypedHttpRequest<?> req = (TypedHttpRequest<?>) request.unwrap();
				try {
					InputStream inputStream = HttpClient.getInputStream(req);
					return new ResponseAdapter(req, inputStream);
				} catch (HttpException e) {
					IOException ex = new IOException("failed to query data "+e.getMessage());
					ex.initCause(e);
					throw ex;
				}
			}

			@Override
			protected void closeConnection(HttpRequest request, HttpResponse response) {
				InputStream inputStream = (InputStream) response.unwrap();
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException ignored) {
					}
				}
			}
		}; 
	}

	/**
	 * Queries the service provider for a request token.
	 * 
	 * @param callbackUrl Pass an actual URL if your app can receive callbacks and you want
	 *        to get informed about the result of the authorization process.
	 *        Pass {@link OAuth.OUT_OF_BAND} if the service provider implements
	 *        OAuth 1.0a and your app cannot receive callbacks. Pass null if the
	 *        service provider implements OAuth 1.0 and your app cannot receive
	 *        callbacks. Please note that some services (among them Twitter)
	 *        will fail authorization if you pass a callback URL but register
	 *        your application as a desktop app (which would only be able to
	 *        handle OOB requests).
	 * @param customOAuthParams you can pass custom OAuth parameters here which will go directly 
	 *        into the signer, i.e. you don't have to put them into the request 
	 *        first. This is useful for pre-setting OAuth params for signing. 
	 *        Pass them sequentially in key/value order. 
	 * @return The URL to which the user must be sent in order to authorize the consumer. It includes the unauthorized request token (and in the case of OAuth 1.0, the callback URL -- 1.0a clients send along with the token request).
	 * @throws OAuthException
	 */
	public String retrieveRequestToken(String callbackUrl, String... customOAuthParams) throws OAuthException {
		return provider.retrieveRequestToken(consumer, callbackUrl, customOAuthParams);
	}

	/**
	 * Queries the service provider for an access token.
	 * 
	 * @param oauthVerifier <b>NOTE: Only applies to service providers implementing OAuth
	 *        1.0a. Set to null if the service provider is still using OAuth
	 *        1.0.</b> The verification code issued by the service provider
	 *        after the the user has granted the consumer authorization. If the
	 *        callback method provided in the previous step was
	 *        {@link OAuth.OUT_OF_BAND}, then you must ask the user for this
	 *        value. If your app has received a callback, the verfication code
	 *        was passed as part of that request instead.
	 * @param customOAuthParams you can pass custom OAuth parameters here which will go directly
	 *        into the signer, i.e. you don't have to put them into the request
	 *        first. This is useful for pre-setting OAuth params for signing.
	 *        Pass them sequentially in key/value order.
	 * @throws OAuthException
	 */
	public void retrieveAccessToken(String oauthVerifier, String... customOAuthParams) throws OAuthException {
		provider.retrieveAccessToken(consumer, oauthVerifier, customOAuthParams);
	}

	/**
	 * Any additional non-OAuth parameters returned in the response body of a
	 * token request can be obtained through this method. These parameters will
	 * be preserved until the next token request is issued. The return value is
	 * never null.
	 */
	public HttpParameters getResponseParameters() {
		return provider.getResponseParameters();
	}

	/**
	 * Get the {@link HttpClientOAuth1Consumer} class used to retrieved the tokens
	 * <p>Might be a {@link OAuth1ConsumerClocked}
	 * @return
	 */
	public HttpClientOAuth1Consumer getConsumer() {
		return consumer;
	}
}
