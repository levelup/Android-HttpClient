package com.levelup.http.ion.internal;

/**
 * Created by robUx4 on 19/08/2014.
 */
public class ErrorDataException extends RuntimeException {
	public Object errorResult;

	public ErrorDataException(Object result) {this.errorResult = result;}
}
