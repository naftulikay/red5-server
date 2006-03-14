package org.red5.server.api;

public interface IClientRegistry {

	public boolean hasClient(String id);
	public IClient newClient();
	public IClient lookupClient(String id);

}