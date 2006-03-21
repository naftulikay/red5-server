package org.red5.server.net;

import org.red5.server.api.IConnection;
import org.red5.server.ex.AccessDeniedException;
import org.red5.server.ex.SharedObjectException;
import org.red5.server.net.message.ISharedObject;

public interface ISharedObjectHandler {
	
	public void handleSharedObject(IConnection conn, ISharedObject so)
		throws AccessDeniedException, SharedObjectException;

}
