package co.tophe.signed;

/**
 * Interface to define an OAuth client.
 */
public interface OAuthClientApp {

	/**
	 * Get the consumer key for the OAuth client.
	 */
	String getConsumerKey();

	/**
	 * Get the consumer secret for the OAuth client.
	 */
	String getConsumerSecret();

}
