package org.red5.server.net;

import org.red5.server.api.IConnection;

public interface IConnectionHandler {

	public boolean connect(IConnection conn) throws AccessDeniedException;
	
	public void disconnect(IConnection conn);
	
}
