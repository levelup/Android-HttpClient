package com.levelup.http;

public interface RequestSigner {

	public abstract void sign(HttpEngine<?> req) throws HttpException;
}
