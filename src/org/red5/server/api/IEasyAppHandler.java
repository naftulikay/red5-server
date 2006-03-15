package org.red5.server.api;

public interface IEasyAppHandler {

	public void ezAppStart(IScope app);

	public void ezAppConnect(IConnection conn);
	
	public void ezAppJoin(IClient client, IScope app);

	public void ezAppLeave(IClient client, IScope app);

	public void ezAppDisconnect(IConnection conn);

	public void ezAppStop(IScope app);

	public void ezRoomStart(IScope room);
	
	public void ezRoomConnect(IConnection conn);

	public void ezRoomJoin(IClient client, IScope room);

	public void ezRoomLeave(IClient client, IScope room);

	public void ezRoomDisconnect(IConnection conn);

	public void ezRoomStop(IScope room);
	
}
