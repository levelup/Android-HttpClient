package com.levelup.http.parser;

import java.io.IOException;

import com.levelup.http.DataErrorException;
import com.levelup.http.ImmutableHttpRequest;
import com.levelup.http.ParserException;

/**
 * Transform data coming from the network from {@link INPUT} to {@link OUTPUT} type
 *
 * @author Created by robUx4 on 20/08/2014.
 */
public interface XferTransform<INPUT, OUTPUT> {
	/**
	 * Transform the network data from {@link INPUT} to {@link OUTPUT}
	 * @param input Input data
	 * @param request The network request it corresponds to
	 * @return Transformed data
	 * @throws IOException
	 * @throws ParserException
	 * @throws DataErrorException
	 */
	OUTPUT transform(INPUT input, ImmutableHttpRequest request) throws IOException, ParserException;
}
