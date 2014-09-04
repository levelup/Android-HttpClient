package com.levelup.http.ion.internal;

import com.koushikdutta.async.http.body.StringBody;
import com.koushikdutta.ion.builder.Builders;
import com.levelup.http.body.HttpBodyString;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonHttpBodyString extends HttpBodyString implements IonBody {

	public IonHttpBodyString(HttpBodyString sourceBody) {
		super(sourceBody);
	}

	@Override
	public String getContentType() {
		return StringBody.CONTENT_TYPE;
	}

	@Override
	public void setOutputData(Builders.Any.B requestBuilder) {
		requestBuilder.setStringBody(value);
	}

	private class TypedStringBody extends StringBody {
		// TODO use this body to use our own Content-Type and get rid of the getContentType() overriding
		@Override
		public String getContentType() {
			return IonHttpBodyString.this.getContentType();
		}
	}
}
