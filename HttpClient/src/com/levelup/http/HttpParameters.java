package com.levelup.http;


/**
 * Holds parameters passed to the HTTP query
 */
public interface HttpParameters {
	/**
	 * Add a String parameter for the HTTP query
	 * @param name Name of the parameter
	 * @param value Value of the parameter
	 */
	void add(String name, String value);

	/**
	 * Add a boolean parameter for the HTTP query
	 * @param name Name of the parameter
	 * @param value Value of the parameter
	 */
	void add(String name, boolean value);

	/**
	 * Add an int parameter for the HTTP query
	 * @param name Name of the parameter
	 * @param value Value of the parameter
	 */
	void add(String name, int value);

	/**
	 * Add a long parameter for the HTTP query
	 * @param name Name of the parameter
	 * @param value Value of the parameter
	 */
	void add(String name, long value);
}
