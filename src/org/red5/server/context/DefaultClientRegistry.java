package org.red5.server.context;

import java.util.HashMap;

import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.impl.Client;
import org.red5.server.net.AccessDeniedException;
import org.red5.server.net.ClientNotFoundException;
import org.red5.server.net.HostNotFoundException;
import org.red5.server.net.ScopeNotFoundException;
import org.springframework.context.ApplicationContext;

public class DefaultClientRegistry implements IClientRegistry {

	private ApplicationContext appCtx = null;
	private HashMap clients = new HashMap();
	private Integer sessionIdCounter = new Integer(0);

	public void setServiceContext(ApplicationContext appCtx){
		this.appCtx = appCtx;
	}
	
	public void registerClient(IClient client) {
		clients.put(client.getId(), client);
	}

	public void unregisterClient(IClient client) {
		clients.remove(client.getId());
	}

	public IClient newClient(String host) throws HostNotFoundException {
		Integer sid;
		synchronized (sessionIdCounter) {
			sid = sessionIdCounter;
			sessionIdCounter = new Integer(sid.intValue() + 1);
		}
		
		IClient client = new Client(sid.toString(), host);
		registerClient(client);
		return client;
	}

	public IClient lookupClient(String sessionId) throws ClientNotFoundException {
		if (!clients.containsKey(sessionId))
			throw new ClientNotFoundException();
		
		return (IClient) clients.get(sessionId);
	}

	public IScope lookupScope(IClient client, String contextPath)
			throws ScopeNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean connect(IConnection conn) throws AccessDeniedException {
		// TODO Auto-generated method stub
		return false;
	}

	public void disconnect(IConnection conn) {
		// TODO Auto-generated method stub

	}

}
