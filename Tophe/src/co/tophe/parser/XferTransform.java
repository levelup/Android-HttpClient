package co.tophe.parser;

import java.io.IOException;

import co.tophe.ImmutableHttpRequest;

/**
 * Transform data coming from the network from {@code INPUT} to {@code OUTPUT} type.
 * <p>You may use {@link co.tophe.parser.Transformer} for transformations that don't need to know about the HTTP request and don't
 * throw exceptions when parsing the data.</p>
 *
 * @param <INPUT>  the input type
 * @param <OUTPUT> the output type
 * @author Created by robUx4 on 20/08/2014.
 * @see co.tophe.parser.XferTransformChain
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
