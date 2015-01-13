package co.tophe.parser;

import java.io.IOException;

import co.tophe.ImmutableHttpRequest;

/**
 * Transform data coming from the network from {@code INPUT} to {@code OUTPUT} type
 *
 * @param <INPUT>  Input type
 * @param <OUTPUT> Output type
 * @author Created by robUx4 on 20/08/2014.
 */
public interface XferTransform<INPUT, OUTPUT> {
	/**
	 * Transform the network data from {@link INPUT} to {@link OUTPUT}
	 *
	 * @param input   Input data
	 * @param request HTTP request that generated the {@code input}
	 * @return the {@link INPUT} data transformed into {@link OUTPUT} data.
	 * @throws IOException
	 * @throws ParserException
	 */
	OUTPUT transformData(INPUT input, ImmutableHttpRequest request) throws IOException, ParserException;
}
