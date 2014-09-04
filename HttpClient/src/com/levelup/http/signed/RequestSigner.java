package com.levelup.http.signed;

import com.levelup.http.HttpEngine;
import com.levelup.http.HttpException;

public interface RequestSigner {

	public abstract void sign(HttpEngine<?> req) throws HttpException;
}
