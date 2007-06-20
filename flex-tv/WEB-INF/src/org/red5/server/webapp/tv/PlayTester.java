package org.red5.server.webapp.tv;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.api.stream.IServerStream;

public class PlayTester extends TimerTask {
	protected static Log log = LogFactory.getLog(Application.class.getName());
	private IServerStream serverStream;
	private Application app;
	@Override
	public void run() {
		//log.debug(serverStream.getCurrentItemIndex());
		if (serverStream.getCurrentItemIndex()==-1)
		{
			finish();
		}
	}
	
	
	public void setApplication(Application app)
	{
		this.serverStream = app.serverStream;
		this.app = app;
	}
	
	private void finish()
	{
		app.updatePlaylist();
	}

}
