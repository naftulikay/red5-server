package org.red5.server.net;

import org.red5.server.api.Client;
import org.red5.server.api.Connection;
import org.red5.server.api.Scope;

public interface IConnectionHandler {

	public Client newClient();
	
	public Client lookupClient(String sessionId);
	
	public Scope  lookupScope(String host, String contextPath);
	
	public boolean connect(Connection conn);
	
	public void disconnect(Connection conn);
	
}
