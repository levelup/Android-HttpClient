package co.tophe.signed;

import co.tophe.HttpAuthException;
import co.tophe.HttpEngine;

public interface RequestSigner {

	public abstract void sign(HttpEngine<?,?> req) throws HttpAuthException;
}
