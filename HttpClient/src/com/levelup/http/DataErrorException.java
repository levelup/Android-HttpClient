package com.levelup.http;

/**
 * Created by robUx4 on 19/08/2014.
 */
public class DataErrorException extends Exception {
	public final Object errorContent;

	public DataErrorException(Object errorContent, Exception cause) {
		super(cause);
		this.errorContent = errorContent;
	}

	@Override
	public String toString() {
		return super.toString() + " errorData:"+errorContent;
	}
}
