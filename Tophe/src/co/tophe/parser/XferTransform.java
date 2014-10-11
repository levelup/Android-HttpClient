package co.tophe.parser;

import java.io.IOException;

import co.tophe.ImmutableHttpRequest;

/**
 * Transform data coming from the network from {@link INPUT} to {@link OUTPUT} type
 *
 * @author Created by robUx4 on 20/08/2014.
 */
public interface XferTransform<INPUT, OUTPUT> {
	/**
	 * Transform the network data from {@link INPUT} to {@link OUTPUT}
	 * @param input Input data
	 * @param request HTTP request that generated the {@code input}
	 * @return Transformed data
	 * @throws IOException
	 * @throws ParserException
	 */
	OUTPUT transformData(INPUT input, ImmutableHttpRequest request) throws IOException, ParserException;
}
