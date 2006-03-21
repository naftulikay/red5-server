package org.red5.server.net;

import java.util.List;

import org.red5.server.api.IConnection;
import org.red5.server.ex.AccessDeniedException;
import org.red5.server.net.message.IEvent;

public interface IEventHandler {

	public void handleEvent(IConnection conn, IEvent event) throws AccessDeniedException;
	public boolean hasEventsWaiting(IConnection conn);
	public List pickupEvent(IConnection conn);
	
}
