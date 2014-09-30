package com.levelup.http;


import android.support.annotation.NonNull;

/**
 * Holds parameters passed to the HTTP query
 */
public interface HttpParameters {
	/**
	 * Add a String parameter for the HTTP query
	 * @param name Name of the parameter
	 * @param value Value of the parameter
	 */
	void add(@NonNull String name, String value);

	/**
	 * Add a boolean parameter for the HTTP query
	 * @param name Name of the parameter
	 * @param value Value of the parameter
	 */
	void add(@NonNull String name, boolean value);

	/**
	 * Add an int parameter for the HTTP query
	 * @param name Name of the parameter
	 * @param value Value of the parameter
	 */
	void add(@NonNull String name, int value);

	/**
	 * Add a long parameter for the HTTP query
	 * @param name Name of the parameter
	 * @param value Value of the parameter
	 */
	void add(@NonNull String name, long value);
}
