package org.red5.server.net;

import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.Scope;

public interface IConnectionHandler {

	public IClient newClient(String host) throws HostNotFoundException;
	
	public IClient lookupClient(String sessionId) throws ClientNotFoundException;
	
	public Scope  lookupScope(IClient client, String contextPath) throws ScopeNotFoundException;
	
	public boolean connect(IConnection conn) throws AccessDeniedException;
	
	public void disconnect(IConnection conn);
	
}
