package co.tophe.signed;

/**
 * Asbtract class to sign for an {@link co.tophe.signed.OAuthUser}.
 */
public abstract class AbstractOAuthSigner implements RequestSigner {

	private final OAuthUser user;

	protected AbstractOAuthSigner(OAuthUser user) {
		this.user = user;
	}
	
	public OAuthUser getOAuthUser() {
		return user;
	}
	
}
