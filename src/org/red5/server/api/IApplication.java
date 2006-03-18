package org.red5.server.api;

public interface IApplication {

	public boolean appStart(IScope app);

	public boolean appConnect(IConnection conn);
	
	public boolean appJoin(IClient client, IScope app);

	public void appLeave(IClient client, IScope app);

	public void appDisconnect(IConnection conn);

	public void appStop(IScope app);

	public boolean roomStart(IScope room);
	
	public boolean roomConnect(IConnection conn);

	public boolean roomJoin(IClient client, IScope room);

	public void roomLeave(IClient client, IScope room);

	public void roomDisconnect(IConnection conn);

	public void roomStop(IScope room);
	
}
