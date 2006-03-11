package org.red5.server.net;

import org.red5.server.api.Client;
import org.red5.server.api.IConnection;
import org.red5.server.api.Scope;

public interface IConnectionHandler {

	public Client newClient(String host) throws HostNotFoundException;
	
	public Client lookupClient(String sessionId) throws ClientNotFoundException;
	
	public Scope  lookupScope(Client client, String contextPath) throws ScopeNotFoundException;
	
	public boolean connect(IConnection conn) throws AccessDeniedException;
	
	public void disconnect(IConnection conn);
	
}
