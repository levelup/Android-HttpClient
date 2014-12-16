package co.tophe;

import android.support.annotation.Nullable;

/**
 * Created by Steve Lhomme on 14/07/2014.
 */
public interface ImmutableHttpRequest {
	HttpRequestInfo getHttpRequest();
	@Nullable HttpResponse getHttpResponse();
}
