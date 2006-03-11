package org.red5.server.net;

import org.red5.server.api.IConnection;
import org.red5.server.net.message.IServiceCall;

public interface IServiceHandler {
	
	public void handleServiceCall(IConnection conn, IServiceCall serviceCall)
		throws ServiceNotFoundException, AccessDeniedException;
	
}