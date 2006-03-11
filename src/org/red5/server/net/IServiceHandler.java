package org.red5.server.net;

import org.red5.server.api.Connection;
import org.red5.server.net.message.IServiceCall;

public interface IServiceHandler {
	
	public void handleServiceCall(Connection conn, IServiceCall serviceCall);
	
}