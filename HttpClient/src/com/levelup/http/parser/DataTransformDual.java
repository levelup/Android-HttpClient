package com.levelup.http.parser;

import java.io.IOException;

import com.levelup.http.DataErrorException;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;
import com.levelup.http.ParserTransform;

/**
 * Created by robUx4 on 20/08/2014.
 */
public class DataTransformDual<INPUT, OUTPUT, MIDDLE> implements DataTransform<INPUT, OUTPUT> {

	private final DataTransform<INPUT, MIDDLE> parserUp;
	private final DataTransform<MIDDLE, OUTPUT> parserDown;

	public DataTransformDual(DataTransform<INPUT, MIDDLE> parserUp, DataTransform<MIDDLE, OUTPUT> parserDown) {
		this.parserUp = parserUp;
		this.parserDown = parserDown;
	}

	@Override
	public OUTPUT transform(INPUT input, ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException {
		MIDDLE intermediate = parserUp.transform(input, request);
		return parserDown.transform(intermediate, request);
	}
}
