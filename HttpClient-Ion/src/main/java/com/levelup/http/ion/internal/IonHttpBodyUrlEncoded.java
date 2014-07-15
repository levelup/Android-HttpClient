package com.levelup.http.ion.internal;

import org.apache.http.NameValuePair;

import com.koushikdutta.async.http.body.UrlEncodedFormBody;
import com.koushikdutta.ion.builder.Builders;
import com.levelup.http.HttpBodyUrlEncoded;

/**
 * Created by Steve Lhomme on 15/07/2014.
 */
public class IonHttpBodyUrlEncoded extends HttpBodyUrlEncoded implements IonBody{
	public IonHttpBodyUrlEncoded(HttpBodyUrlEncoded sourceBody) {
		super(sourceBody);
	}

	@Override
	public String getContentType() {
		return UrlEncodedFormBody.CONTENT_TYPE;
	}

	@Override
	public void setOutputData(Builders.Any.B requestBuilder) {
		for (NameValuePair param : mParams) {
			requestBuilder.setBodyParameter(param.getName(), param.getValue());
		}
	}
}
