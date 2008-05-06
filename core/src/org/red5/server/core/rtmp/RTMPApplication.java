package org.red5.server.core.rtmp;

import org.red5.server.common.service.ServiceRegistry;

public interface RTMPApplication {
	String getName();
	ServiceRegistry getRegistry();
	RTMPApplicationInstance acquireInstance(String instanceName);
	void releaseInstance(String instanceName);
}
