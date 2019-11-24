package es.us.isa.ideas.repo;

public class DummyAuthenticationManagerDelegate implements AuthenticationManagerDelegate {

	private String user;
	
	public DummyAuthenticationManagerDelegate(String user){
		this.user=user;
	}
	
	@Override
	public String getAuthenticatedUserId() {
		return user;
	}

	@Override
	public boolean operationAllowed(String authenticatedUser, String Owner, String workspace, String project,
			String fileOrDirectoryUri, AuthOpType operationType) {
		// TODO Auto-generated method stub
		return true;
	}

}
