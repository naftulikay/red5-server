package org.red5.server.net;

import java.util.List;

import org.red5.server.api.Connection;
import org.red5.server.net.message.IEvent;

public interface IEventHandler {

	public void handleEvent(Connection conn, IEvent event);
	public boolean hasEventsWaiting(Connection conn);
	public List pickupEvent(Connection conn);
	
}
