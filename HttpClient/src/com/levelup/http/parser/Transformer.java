package com.levelup.http.parser;

import java.io.IOException;

import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * A more generic {@link com.levelup.http.parser.XferTransform} that doesn't throw {@link java.io.IOException} or {@link com.levelup.http.ParserException}
 * to be used when the network data are handled and only post-processing is needed
 *
 * @author Created by robUx4 on 21/08/2014.
 */
public abstract class Transformer<INPUT, OUTPUT> implements XferTransform<INPUT, OUTPUT> {

	/**
	 * Transform the data from {@link INPUT} to {@link OUTPUT}
	 * @param input Input data
	 * @param request HTTP request that generated the {@code input}
	 * @return Transformed data
	 */
	protected abstract OUTPUT transform(INPUT input, ImmutableHttpRequest request);

	@Deprecated
	@Override
	public final OUTPUT transformData(INPUT input, ImmutableHttpRequest request) throws IOException, ParserException {
		return transform(input, request);
	}
}
