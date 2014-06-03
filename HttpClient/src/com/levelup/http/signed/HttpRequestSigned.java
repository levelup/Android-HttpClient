package com.levelup.http.signed;

import com.levelup.http.HttpRequest;

public interface HttpRequestSigned extends HttpRequest {

	OAuthUser getOAuthUser();

}
