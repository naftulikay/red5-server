package org.red5.server.context;

import org.red5.server.api.IClient;
import org.red5.server.net.IConnectionHandler;

public interface IClientRegistry extends IConnectionHandler {

	public void registerClient(IClient client);
	public void unregisterClient(IClient client);
}
