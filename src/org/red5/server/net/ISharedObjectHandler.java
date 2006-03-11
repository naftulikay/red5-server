package org.red5.server.net;

import org.red5.server.api.Connection;
import org.red5.server.net.message.ISharedObject;

public interface ISharedObjectHandler {
	
	public void handleSharedObject(Connection conn, ISharedObject so);

}
