package org.red5.server.context;

import java.util.List;

import org.red5.server.api.IClient;

public interface AppLifecycleAware {
	
	public void onAppStart();

	public void onAppStop();
	
	public boolean onConnect(IClient client, List params);
	
	public void onDisconnect(IClient client);

}
