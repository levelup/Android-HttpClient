package com.levelup.http.parser;

import java.io.IOException;

import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * Created by robUx4 on 21/08/2014.
 */
public abstract class ResultTransform<INPUT, OUTPUT> implements XferTransform<INPUT, OUTPUT> {

	protected abstract OUTPUT transformResult(INPUT result, ImmutableHttpRequest request);

	@Override
	public final OUTPUT transform(INPUT input, ImmutableHttpRequest request) throws IOException, ParserException {
		return transformResult(input, request);
	}
}
