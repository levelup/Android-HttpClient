package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * Transform the result from an {@link com.levelup.http.InputStreamParser} into another type
 *
 * Created by robUx4 on 19/08/2014.
 */
public abstract class ParserTransform<INPUT, OUTPUT> implements InputStreamParser<OUTPUT> {
	private final InputStreamParser<INPUT> inputParser;

	public ParserTransform(InputStreamParser<INPUT> inputParser) {
		this.inputParser = inputParser;
	}

	protected abstract OUTPUT transform(INPUT input) throws IOException, ParserException;

	@Override
	public final OUTPUT parseInputStream(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException {
		INPUT input = inputParser.parseInputStream(inputStream, request);
		return transform(input);
	}

	@Override
	public GsonStreamParser<OUTPUT> getGsonParser() {
		// TODO: shortcut for GSON handling is not supported for now as it will not run the transform
		return null;
	}
}
