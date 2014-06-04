package com.levelup.http;

public interface RequestSigner {

	public abstract void sign(HttpRequest req) throws HttpException;
}
