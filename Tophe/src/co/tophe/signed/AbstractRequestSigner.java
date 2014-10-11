package co.tophe.signed;


public abstract class AbstractRequestSigner implements RequestSigner {

	private final OAuthUser user;

	protected AbstractRequestSigner(OAuthUser user) {
		this.user = user;
	}
	
	public OAuthUser getOAuthUser() {
		return user;
	}
	
}
