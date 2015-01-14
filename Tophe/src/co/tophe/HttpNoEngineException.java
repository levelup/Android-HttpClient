package co.tophe;

import android.support.annotation.NonNull;

/**
 * Exception raised when no {@link HttpEngine} could be found to process the {@link co.tophe.HttpRequestInfo}.
 */
public class HttpNoEngineException extends HttpRequestException{
	protected HttpNoEngineException(@NonNull Builder builder) {
		super(builder);
	}

	public static class Builder extends AbstractBuilder<HttpNoEngineException, Builder> {
		public Builder(@NonNull HttpRequestInfo httpRequest) {
			super(httpRequest);
		}

		@Override
		public HttpNoEngineException build() {
			return new HttpNoEngineException(this);
		}
	}
}
