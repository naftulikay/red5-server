package org.red5.server.app;

import org.red5.server.core.rtmp.RTMPApplicationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestApplicationFwMain {
	private static final Logger log = LoggerFactory.getLogger(TestApplicationFwMain.class);
	
	private RTMPApplicationManager appManager;
	
	public void start() {
		log.info("Test App FW Start");
		TestApplication app = new TestApplication("TestApp");
		appManager.registerApplication(app, null);
	}
	
	public void stop() {
		appManager.unregisterApplication("TestApp");
		log.info("Test App FW Stop");
	}

	public void setAppManager(RTMPApplicationManager appManager) {
		this.appManager = appManager;
	}
	
}
