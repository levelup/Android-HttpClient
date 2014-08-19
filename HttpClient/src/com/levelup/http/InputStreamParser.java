package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface to turn an {@link InputStream} into an object of type {@code <T>} by parsing the data read from the stream.
 * @see InputStreamStringParser
 *
 * @param <T> Type of the output object
 */
public interface InputStreamParser<T> {

	/**
	 * Parse the data read from the {@code inputStream} and turn them into an object of type {@code T}
	 * @param inputStream The input stream to read the data from
	 * @param request
	 * @return The object corresponding to the parsed data
	 * @throws ParserException When the data parsing fails for some reason
	 * @throws IOException
	 */
	T parseInputStream(InputStream inputStream, ImmutableHttpRequest request) throws IOException, ParserException, DataErrorException;

	GsonStreamParser<T> getGsonParser();
}
