package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.parser.DataTransform;

/**
 * Transform the result from an {@link com.levelup.http.InputStreamParser} into another type
 *
 * Created by robUx4 on 19/08/2014.
 */
public abstract class ParserTransform<INPUT, OUTPUT> implements InputStreamParser<OUTPUT>, DataTransform<INPUT, OUTPUT> {
	private final InputStreamParser<INPUT> inputParser;

	public ParserTransform(InputStreamParser<INPUT> inputParser) {
		this.inputParser = inputParser;
	}

	@Override
	public final OUTPUT parseInputStream(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException {
		INPUT input = inputParser.parseInputStream(inputStream, request);
		return transform(input, request);
	}
}
