package com.levelup.http.parser;

import java.io.IOException;

import com.levelup.http.DataErrorException;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * Created by robUx4 on 20/08/2014.
 */
public interface DataTransform<INPUT, OUTPUT> {
	OUTPUT transform(INPUT input, ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException;
}
