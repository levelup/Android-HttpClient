package com.levelup.http.signed;

import com.levelup.http.HttpAuthException;
import com.levelup.http.HttpEngine;

public interface RequestSigner {

	public abstract void sign(HttpEngine<?,?> req) throws HttpAuthException;
}
