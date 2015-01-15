package co.tophe.signed;

import co.tophe.HttpSignException;
import co.tophe.HttpEngine;

/**
 * Interface for signing a request.
 *
 * @see co.tophe.signed.RequestSignerOAuth2
 */
public interface RequestSigner {

	/**
	 * Add signature headers for the specified {@link co.tophe.HttpEngine}
	 *
	 * @param req engine to sign before the request is processed.
	 * @throws HttpSignException
	 * @see co.tophe.HttpEngine#setHeader(String, String)
	 */
	void sign(HttpEngine<?, ?> req) throws HttpSignException;
}
