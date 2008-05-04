package org.red5.server.core.rtmp;

import java.util.Collection;

import org.red5.server.common.service.ServiceRegistry;

public interface RTMPApplicationInstance {
	String getInstanceName();
	RTMPApplication getApplication();
	ServiceRegistry getInstanceRegistry();
	Collection<RTMPConnection> getConnections();
}
