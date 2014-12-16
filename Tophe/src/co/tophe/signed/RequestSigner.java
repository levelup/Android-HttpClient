package co.tophe.signed;

import co.tophe.HttpSignException;
import co.tophe.HttpEngine;

public interface RequestSigner {

	public abstract void sign(HttpEngine<?,?> req) throws HttpSignException;
}
