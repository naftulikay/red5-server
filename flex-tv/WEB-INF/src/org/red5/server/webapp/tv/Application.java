package org.red5.server.webapp.tv;

import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.adapter.*;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.stream.IServerStream;
import org.red5.server.api.stream.support.SimplePlayItem;
//import org.red5.server.api.stream.IBroadcastStream;


import org.red5.server.api.stream.support.StreamUtils;
import org.springframework.core.io.Resource;


public class Application extends ApplicationAdapter {
	public IScope appScope;
	public IServerStream serverStream;
	private SimplePlayItem item;
	protected static Log log = LogFactory.getLog(Application.class.getName());

	/** {@inheritDoc} */
    @Override
	public boolean appStart(IScope app) {
    	Timer timer = new Timer();
    	log.debug("Application start");
    	appScope = app;
		serverStream = StreamUtils.createServerStream(appScope, "live0");
		getList();
		serverStream.start();
		PlayTester plTester = new PlayTester();
		plTester.setApplication(this);
		timer.schedule(plTester, 0, 500);
		
		return true;
	}
    
	/** {@inheritDoc} */
    @Override
	public boolean appConnect(IConnection conn, Object[] params) {
		measureBandwidth(conn);
		log.debug("Client: "+conn.getHost()+" connected");
		log.debug("ID: "+conn.getClient().getId());
		return super.appConnect(conn, params);
	}

	/** {@inheritDoc} */
    @Override
	public void appDisconnect(IConnection conn) {
    	log.debug("Client: "+conn.getHost()+" disconnected");
		super.appDisconnect(conn);
	}
    
    @Override
    public void appStop(IScope app) {
    	if (appScope == app && serverStream != null) {
			serverStream.close();
		}
    	super.appStop(app);
    }

    public synchronized  boolean getList() {
		try {
			log.debug("getting the FLV files");
			Resource[] flvs = appScope.getResources("streams/*.flv");
			if (flvs != null) {
				serverStream.removeAllItems();
				for (Resource flv : flvs) {
					String flvName = flv.getFile().getName();
					item = new SimplePlayItem();
					item.setName(flvName);
					serverStream.addItem(item);					
					log.debug("flvName: " + flvName);
				}
			}
		}catch (Exception e) {
			log.error(e);
		}
		return true;
	}
    
    public void updatePlaylist()
    {
		getList();
		serverStream.setItem(0);
    }
	    
}
