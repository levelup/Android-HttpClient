package co.tophe;

import android.support.annotation.NonNull;

/**
 * Raw HTTP header class.
 */
public class Header {
	
	private final String name;
	private final String value;

	public Header(@NonNull String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Get the HTTP header name.
	 */
	@NonNull
	public String getName() {
		return name;
	}

	/**
	 * Get the HTTP header value.
	 */
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
        return "{" + name + ':' + value + '}';
	}
	
}