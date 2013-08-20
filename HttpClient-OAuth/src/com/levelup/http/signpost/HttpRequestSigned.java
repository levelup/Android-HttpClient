package com.levelup.http.signpost;

import com.levelup.http.HttpRequest;

public interface HttpRequestSigned extends HttpRequest {

	OAuthUser getOAuthUser();

}
